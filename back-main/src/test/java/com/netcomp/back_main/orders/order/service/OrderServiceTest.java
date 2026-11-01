package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.orders.order.api.request.CreateGuestOrderRequest;
import com.netcomp.back_main.orders.order.api.request.CreateOrderRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusRepository;
import com.netcomp.back_main.support.TestFixtures;
import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.repository.CartRepository;
import com.netcomp.back_main.users.user.domain.Role;
import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderInventoryService orderInventoryService;

    @Mock
    private EntityManager entityManager;

    private OrderService orderService;
    private User user;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderStatusRepository,
                cartRepository,
                orderInventoryService,
                entityManager
        );
        user = TestFixtures.user();
        lenient().when(entityManager.find(User.class, user.getId())).thenReturn(user);
        lenient().when(orderInventoryService.lockProduct(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        for (OrderStatusCode code : OrderStatusCode.values()) {
            lenient().when(orderStatusRepository.findById(code.name()))
                    .thenReturn(Optional.of(TestFixtures.status(code)));
        }
    }

    @Test
    void createsPendingOrderFromCartAndClearsIt() {
        Product product = TestFixtures.product("Monitor Samsung", "650.50", 4);
        Cart cart = TestFixtures.cart(user, product, 3);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResponse response = orderService.createOrder(
                user.getId(),
                new CreateOrderRequest("  Av. Javier Prado 456, San Isidro  ")
        );

        assertEquals("PENDING", response.status());
        assertEquals(new BigDecimal("1951.50"), response.total());
        assertEquals(3, response.itemCount());
        assertEquals("Av. Javier Prado 456, San Isidro", response.shippingAddress());
        assertEquals("Monitor Samsung", response.items().get(0).productName());
        assertEquals(8, response.code().length());
        assertTrue(cart.getItems().isEmpty());
        verify(orderInventoryService).registerSale(any(Order.class));
    }

    @Test
    void rejectsCheckoutWithEmptyCart() {
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(user.getId(), new CreateOrderRequest("Av. Brasil 1000, Lima")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsCheckoutWhenStockChangedSinceAddingToCart() {
        Product product = TestFixtures.product("Procesador Ryzen", "1200.00", 1);
        Cart cart = TestFixtures.cart(user, product, 2);
        when(cartRepository.findWithItemsByUserId(user.getId())).thenReturn(Optional.of(cart));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(user.getId(), new CreateOrderRequest("Av. Brasil 1000, Lima")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Procesador Ryzen (disponibles: 1)"));
        verify(orderInventoryService, never()).registerSale(any());
        assertEquals(1, cart.getItems().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void createsGuestOrderAndRegistersNewCustomer() {
        Product product = TestFixtures.product("Teclado", "150.00", 5);
        Role customerRole = new Role("ROLE_CUSTOMER", "Cliente");
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter(anyString(), any())).thenReturn(userQuery);
        when(userQuery.setMaxResults(1)).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of());
        when(entityManager.find(Role.class, "ROLE_CUSTOMER")).thenReturn(customerRole);
        when(entityManager.find(Product.class, product.getId())).thenReturn(product);
        when(orderRepository.saveAndFlush(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResponse response = orderService.createGuestOrder(new CreateGuestOrderRequest(
                " Luis Pérez ",
                " Luis@Example.com ",
                "Av. Brasil 1000, Lima",
                List.of(new AddToCartRequest(product.getId(), 1), new AddToCartRequest(product.getId(), 1))
        ));

        assertEquals("PENDING", response.status());
        assertEquals(new BigDecimal("300.00"), response.total());
        assertEquals(2, response.itemCount());
        assertEquals("Luis Pérez", response.customerName());
        assertEquals("luis@example.com", response.customerEmail());
        verify(entityManager).persist(any(User.class));
        verify(orderInventoryService).registerSale(any(Order.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rejectsGuestOrderWhenStockIsInsufficient() {
        Product product = TestFixtures.product("Procesador Ryzen", "1200.00", 1);
        TypedQuery<User> userQuery = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter(anyString(), any())).thenReturn(userQuery);
        when(userQuery.setMaxResults(1)).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(List.of(user));
        when(entityManager.find(Product.class, product.getId())).thenReturn(product);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.createGuestOrder(new CreateGuestOrderRequest(
                        "Ana Torres",
                        user.getEmail(),
                        "Av. Brasil 1000, Lima",
                        List.of(new AddToCartRequest(product.getId(), 2))
                )));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(entityManager, never()).persist(any());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void customerCanCancelPendingOrderAndStockIsRestored() {
        Product product = TestFixtures.product("Mouse", "80.00", 5);
        Order order = TestFixtures.order(user, OrderStatusCode.PENDING, product, 1);
        when(orderRepository.findDetailedByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(order)).thenReturn(order);

        OrderResponse response = orderService.cancelUserOrder(user.getId(), order.getId());

        assertEquals("CANCELLED", response.status());
        verify(orderInventoryService).restoreStock(order);
    }

    @Test
    void customerCannotCancelPaidOrder() {
        Product product = TestFixtures.product("Mouse", "80.00", 5);
        Order order = TestFixtures.order(user, OrderStatusCode.PAID, product, 1);
        when(orderRepository.findDetailedByIdAndUserId(order.getId(), user.getId())).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.cancelUserOrder(user.getId(), order.getId()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(orderInventoryService, never()).restoreStock(any());
    }
}
