package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.common.api.request.PageableRequest;
import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.orders.order.api.request.CreateGuestOrderRequest;
import com.netcomp.back_main.orders.order.api.request.CreateOrderRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderItem;
import com.netcomp.back_main.orders.order.domain.OrderStatus;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusRepository;
import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.repository.CartRepository;
import com.netcomp.back_main.users.user.domain.Role;
import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";
    private static final String GUEST_PASSWORD_HASH = "!guest";

    // --- Dependencies ---
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CartRepository cartRepository;
    private final OrderInventoryService orderInventoryService;
    private final EntityManager entityManager;

    // --- Constructor ---
    public OrderService(
            OrderRepository orderRepository,
            OrderStatusRepository orderStatusRepository,
            CartRepository cartRepository,
            OrderInventoryService orderInventoryService,
            EntityManager entityManager
    ) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.cartRepository = cartRepository;
        this.orderInventoryService = orderInventoryService;
        this.entityManager = entityManager;
    }

    // --- Public operations ---
    @Transactional
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        User user = requireActiveUser(userId);
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .filter(existingCart -> !existingCart.getItems().isEmpty())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Tu carrito está vacío."
                ));

        List<OrderLine> lines = cart.getItems().stream()
                .map(item -> new OrderLine(item.getProduct(), item.getQuantity()))
                .toList();
        Order savedOrder = placeOrder(user, lines, request.shippingAddress());
        cart.getItems().clear();
        cartRepository.flush();

        LOGGER.info("Order created. id={}, userId={}, total={}", savedOrder.getId(), userId, savedOrder.getTotal());
        return OrderResponse.from(savedOrder);
    }

    @Transactional
    public OrderResponse createGuestOrder(CreateGuestOrderRequest request) {
        User customer = resolveGuestCustomer(request.customerName(), request.customerEmail());
        Map<UUID, Integer> requestedQuantities = new LinkedHashMap<>();

        for (AddToCartRequest item : request.items()) {
            requestedQuantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        List<OrderLine> lines = new ArrayList<>();
        requestedQuantities.forEach((productId, quantity) -> {
            Product product = entityManager.find(Product.class, productId);
            if (product == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Uno de los productos de tu carrito ya no está disponible."
                );
            }
            lines.add(new OrderLine(product, quantity));
        });

        Order savedOrder = placeOrder(customer, lines, request.shippingAddress());

        LOGGER.info("Guest order created. id={}, userId={}, total={}",
                savedOrder.getId(), customer.getId(), savedOrder.getTotal());
        return OrderResponse.from(savedOrder);
    }

    public PageResponse<OrderResponse> getUserOrders(UUID userId, Integer page, Integer perPage) {
        requireActiveUser(userId);
        int resolvedPage = page == null || page < 1 ? 1 : page;
        PageRequest pageable = PageRequest.of(resolvedPage - 1, PageableRequest.resolvePageSize(perPage));
        return PageResponse.from(orderRepository.findByUserId(userId, pageable), OrderResponse::from);
    }

    public OrderResponse getUserOrder(UUID userId, UUID orderId) {
        return OrderResponse.from(requireUserOrder(userId, orderId));
    }

    @Transactional
    public OrderResponse cancelUserOrder(UUID userId, UUID orderId) {
        Order order = requireUserOrder(userId, orderId);

        if (OrderStatusCode.from(order.getStatus().getCode()) != OrderStatusCode.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo puedes cancelar pedidos pendientes de pago."
            );
        }

        orderInventoryService.restoreStock(order);
        order.setStatus(requireStatus(OrderStatusCode.CANCELLED));

        LOGGER.info("Order cancelled by customer. id={}, userId={}", orderId, userId);
        return OrderResponse.from(orderRepository.saveAndFlush(order));
    }

    // --- Private helpers ---
    private Order placeOrder(User user, List<OrderLine> lines, String shippingAddress) {
        List<OrderLine> sortedLines = lines.stream()
                .sorted(Comparator.comparing(line -> line.product().getId()))
                .toList();
        List<String> unavailableProducts = new ArrayList<>();

        for (OrderLine line : sortedLines) {
            Product product = orderInventoryService.lockProduct(line.product());
            if (!Boolean.TRUE.equals(product.getActive())) {
                unavailableProducts.add(product.getName() + " ya no está disponible");
            } else if (product.getStock() < line.quantity()) {
                unavailableProducts.add(product.getName() + " (disponibles: " + product.getStock() + ")");
            }
        }

        if (!unavailableProducts.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente: " + String.join(", ", unavailableProducts) + "."
            );
        }

        Order order = new Order(
                user,
                requireStatus(OrderStatusCode.PENDING),
                BigDecimal.ZERO,
                shippingAddress.trim()
        );
        BigDecimal total = BigDecimal.ZERO;

        for (OrderLine line : sortedLines) {
            Product product = line.product();
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
            order.getItems().add(new OrderItem(
                    order,
                    product,
                    product.getName(),
                    line.quantity(),
                    product.getPrice(),
                    subtotal
            ));
            total = total.add(subtotal);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.saveAndFlush(order);
        orderInventoryService.registerSale(savedOrder);
        return savedOrder;
    }

    private User resolveGuestCustomer(String name, String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        List<User> existingUsers = entityManager
                .createQuery("SELECT customer FROM User customer WHERE LOWER(customer.email) = :email", User.class)
                .setParameter("email", normalizedEmail)
                .setMaxResults(1)
                .getResultList();

        if (!existingUsers.isEmpty()) {
            User existingUser = existingUsers.get(0);
            if (!Boolean.TRUE.equals(existingUser.getActive())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario está inactivo.");
            }
            return existingUser;
        }

        Role customerRole = entityManager.find(Role.class, CUSTOMER_ROLE);
        if (customerRole == null) {
            throw new IllegalStateException("Missing role: " + CUSTOMER_ROLE);
        }
        User guest = new User(customerRole, name.trim(), normalizedEmail, GUEST_PASSWORD_HASH, true);
        entityManager.persist(guest);
        return guest;
    }

    private Order requireUserOrder(UUID userId, UUID orderId) {
        requireActiveUser(userId);
        return orderRepository.findDetailedByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El pedido no existe."
                ));
    }

    private OrderStatus requireStatus(OrderStatusCode code) {
        return orderStatusRepository.findById(code.name())
                .orElseThrow(() -> new IllegalStateException("Missing order status: " + code));
    }

    private User requireActiveUser(UUID userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe.");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario está inactivo.");
        }
        return user;
    }

    private record OrderLine(Product product, int quantity) {
    }
}
