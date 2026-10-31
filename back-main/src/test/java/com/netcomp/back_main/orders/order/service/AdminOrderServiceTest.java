package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.orders.order.api.request.SalesSummaryRequest;
import com.netcomp.back_main.orders.order.api.request.UpdateOrderStatusRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.api.response.SalesSummaryResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusTotalView;
import com.netcomp.back_main.orders.order.repository.TopProductView;
import com.netcomp.back_main.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderStatusRepository orderStatusRepository;

    @Mock
    private OrderInventoryService orderInventoryService;

    private AdminOrderService adminOrderService;

    @BeforeEach
    void setUp() {
        adminOrderService = new AdminOrderService(orderRepository, orderStatusRepository, orderInventoryService);
        for (OrderStatusCode code : OrderStatusCode.values()) {
            lenient().when(orderStatusRepository.findById(code.name()))
                    .thenReturn(Optional.of(TestFixtures.status(code)));
        }
        lenient().when(orderStatusRepository.findAll()).thenReturn(Arrays.stream(OrderStatusCode.values())
                .map(TestFixtures::status)
                .toList());
    }

    @Test
    void advancesOrderToNextStatus() {
        Order order = TestFixtures.order(TestFixtures.user(), OrderStatusCode.PAID,
                TestFixtures.product("Laptop", "3500.00", 3), 1);
        when(orderRepository.findDetailedById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(order)).thenReturn(order);

        OrderResponse response = adminOrderService.updateStatus(order.getId(), new UpdateOrderStatusRequest("PROCESSING"));

        assertEquals("PROCESSING", response.status());
        assertEquals(List.of("SHIPPED", "CANCELLED"), response.nextStatuses());
        verify(orderInventoryService, never()).restoreStock(any());
    }

    @Test
    void rejectsInvalidTransition() {
        Order order = TestFixtures.order(TestFixtures.user(), OrderStatusCode.PENDING,
                TestFixtures.product("Laptop", "3500.00", 3), 1);
        when(orderRepository.findDetailedById(order.getId())).thenReturn(Optional.of(order));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminOrderService.updateStatus(order.getId(), new UpdateOrderStatusRequest("DELIVERED")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("No se puede cambiar un pedido de Pendiente a Entregado.", exception.getReason());
    }

    @Test
    void cancellingRestoresStock() {
        Product product = TestFixtures.product("Teclado", "199.00", 0);
        Order order = TestFixtures.order(TestFixtures.user(), OrderStatusCode.PROCESSING, product, 2);
        when(orderRepository.findDetailedById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.saveAndFlush(order)).thenReturn(order);

        OrderResponse response = adminOrderService.updateStatus(order.getId(), new UpdateOrderStatusRequest("CANCELLED"));

        assertEquals("CANCELLED", response.status());
        assertTrue(response.nextStatuses().isEmpty());
        verify(orderInventoryService).restoreStock(order);
    }

    @Test
    void failsWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findDetailedById(orderId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminOrderService.getOrder(orderId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void buildsSummaryWithRevenueFromSalesOnlyAndFillsEmptyDays() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 3);
        when(orderRepository.sumTotalsByStatus(any(), any())).thenReturn(List.of(
                new OrderStatusTotalView("PAID", "Pagado", 2L, new BigDecimal("300.00")),
                new OrderStatusTotalView("DELIVERED", "Entregado", 1L, new BigDecimal("100.00")),
                new OrderStatusTotalView("PENDING", "Pendiente", 4L, new BigDecimal("999.00")),
                new OrderStatusTotalView("CANCELLED", "Cancelado", 1L, new BigDecimal("50.00"))
        ));
        when(orderRepository.sumUnitsSold(any(), any(), anyCollection())).thenReturn(7L);
        when(orderRepository.sumDailySales(any(), any(), anyCollection(), anyString())).thenReturn(List.<Object[]>of(
                new Object[]{java.sql.Date.valueOf(LocalDate.of(2026, 10, 2)), 3L, new BigDecimal("400.00")}
        ));
        when(orderRepository.findTopProducts(any(), any(), anyCollection(), any())).thenReturn(List.of(
                new TopProductView(UUID.randomUUID(), "Mouse", 5L, new BigDecimal("250.00"))
        ));

        SalesSummaryResponse summary = adminOrderService.getSummary(new SalesSummaryRequest(from, to));

        assertEquals(new BigDecimal("400.00"), summary.totalRevenue());
        assertEquals(3, summary.completedOrders());
        assertEquals(new BigDecimal("133.33"), summary.averageTicket());
        assertEquals(4, summary.pendingOrders());
        assertEquals(1, summary.cancelledOrders());
        assertEquals(7, summary.unitsSold());
        assertEquals(3, summary.dailySales().size());
        assertEquals(0, summary.dailySales().get(0).orderCount());
        assertEquals(new BigDecimal("400.00"), summary.dailySales().get(1).revenue());
        assertEquals(6, summary.statusBreakdown().size());
        assertEquals("Mouse", summary.topProducts().get(0).productName());
    }

    @Test
    void rejectsInvertedSummaryRange() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminOrderService.getSummary(new SalesSummaryRequest(
                        LocalDate.of(2026, 10, 5),
                        LocalDate.of(2026, 10, 1)
                )));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
