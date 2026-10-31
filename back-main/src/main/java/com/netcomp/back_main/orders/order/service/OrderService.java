package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.common.api.request.PageableRequest;
import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.orders.order.api.request.CreateOrderRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderItem;
import com.netcomp.back_main.orders.order.domain.OrderStatus;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusRepository;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.domain.CartItem;
import com.netcomp.back_main.users.cart.repository.CartRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

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

        List<CartItem> cartItems = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getId()))
                .toList();
        List<String> unavailableProducts = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = orderInventoryService.lockProduct(cartItem.getProduct());
            if (!Boolean.TRUE.equals(product.getActive())) {
                unavailableProducts.add(product.getName() + " ya no está disponible");
            } else if (product.getStock() < cartItem.getQuantity()) {
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
                request.shippingAddress().trim()
        );
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            order.getItems().add(new OrderItem(
                    order,
                    product,
                    product.getName(),
                    cartItem.getQuantity(),
                    product.getPrice(),
                    subtotal
            ));
            total = total.add(subtotal);
        }

        order.setTotal(total);
        Order savedOrder = orderRepository.saveAndFlush(order);
        orderInventoryService.registerSale(savedOrder);
        cart.getItems().clear();
        cartRepository.flush();

        LOGGER.info("Order created. id={}, userId={}, total={}", savedOrder.getId(), userId, total);
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
}
