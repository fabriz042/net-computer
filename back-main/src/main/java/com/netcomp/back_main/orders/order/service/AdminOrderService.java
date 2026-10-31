package com.netcomp.back_main.orders.order.service;

import com.netcomp.back_main.common.api.response.PageResponse;
import com.netcomp.back_main.orders.order.api.request.AdminOrderSearchRequest;
import com.netcomp.back_main.orders.order.api.request.SalesSummaryRequest;
import com.netcomp.back_main.orders.order.api.request.UpdateOrderStatusRequest;
import com.netcomp.back_main.orders.order.api.response.OrderResponse;
import com.netcomp.back_main.orders.order.api.response.OrderStatusResponse;
import com.netcomp.back_main.orders.order.api.response.SalesSummaryResponse;
import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderStatus;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;
import com.netcomp.back_main.orders.order.domain.SalesPeriod;
import com.netcomp.back_main.orders.order.repository.OrderRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusRepository;
import com.netcomp.back_main.orders.order.repository.OrderStatusTotalView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminOrderService {

    // --- Constants ---
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminOrderService.class);
    private static final int SUMMARY_DEFAULT_DAYS = 30;
    private static final int TOP_PRODUCTS_LIMIT = 5;

    // --- Dependencies ---
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderInventoryService orderInventoryService;

    // --- Constructor ---
    public AdminOrderService(
            OrderRepository orderRepository,
            OrderStatusRepository orderStatusRepository,
            OrderInventoryService orderInventoryService
    ) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderInventoryService = orderInventoryService;
    }

    // --- Public operations ---
    public PageResponse<OrderResponse> getOrders(AdminOrderSearchRequest request) {
        SalesPeriod period = resolvePeriod(request.from(), request.to());
        String statusCode = StringUtils.upperCase(StringUtils.trimToNull(request.status()));
        Pageable pageable = PageRequest.of(request.resolvedPage() - 1, request.resolvedPerPage());
        String sortOrder = "asc".equalsIgnoreCase(request.order()) ? "asc" : "desc";

        Page<Order> page = orderRepository.searchOrders(
                period.start(),
                period.end(),
                statusCode == null,
                statusCode == null ? "" : statusCode,
                StringUtils.trimToEmpty(request.search()),
                sortOrder,
                pageable
        );
        return PageResponse.from(page, OrderResponse::from);
    }

    public OrderResponse getOrder(UUID orderId) {
        return OrderResponse.from(requireOrder(orderId));
    }

    public List<OrderStatusResponse> getStatuses() {
        Map<String, OrderStatus> statuses = orderStatusRepository.findAll().stream()
                .collect(Collectors.toMap(OrderStatus::getCode, Function.identity()));
        return Arrays.stream(OrderStatusCode.values())
                .map(code -> statuses.get(code.name()))
                .filter(status -> status != null)
                .map(OrderStatusResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = requireOrder(orderId);
        OrderStatusCode currentStatus = OrderStatusCode.from(order.getStatus().getCode());
        OrderStatusCode targetStatus = OrderStatusCode.from(request.status());

        if (currentStatus == targetStatus) {
            return OrderResponse.from(order);
        }
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede cambiar un pedido de " + order.getStatus().getName()
                            + " a " + requireStatus(targetStatus).getName() + "."
            );
        }
        if (targetStatus == OrderStatusCode.CANCELLED) {
            orderInventoryService.restoreStock(order);
        }

        order.setStatus(requireStatus(targetStatus));
        Order savedOrder = orderRepository.saveAndFlush(order);

        LOGGER.info("Order status updated. id={}, from={}, to={}", orderId, currentStatus, targetStatus);
        return OrderResponse.from(savedOrder);
    }

    public SalesSummaryResponse getSummary(SalesSummaryRequest request) {
        SalesPeriod period = resolveSummaryPeriod(request.from(), request.to());
        List<String> saleStatusCodes = OrderStatusCode.saleStatuses().stream().map(Enum::name).toList();
        List<OrderStatusTotalView> statusTotals = orderRepository.sumTotalsByStatus(period.start(), period.end());

        long completedOrders = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long pendingOrders = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long cancelledOrders = 0;

        for (OrderStatusTotalView statusTotal : statusTotals) {
            OrderStatusCode code = OrderStatusCode.from(statusTotal.code());
            long count = statusTotal.orderCount() == null ? 0 : statusTotal.orderCount();
            BigDecimal total = statusTotal.total() == null ? BigDecimal.ZERO : statusTotal.total();
            if (code.countsAsSale()) {
                completedOrders += count;
                totalRevenue = totalRevenue.add(total);
            } else if (code == OrderStatusCode.PENDING) {
                pendingOrders = count;
                pendingAmount = total;
            } else if (code == OrderStatusCode.CANCELLED) {
                cancelledOrders = count;
            }
        }

        BigDecimal averageTicket = completedOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(completedOrders), 2, RoundingMode.HALF_UP);
        Long unitsSold = orderRepository.sumUnitsSold(period.start(), period.end(), saleStatusCodes);

        return new SalesSummaryResponse(
                period.from(),
                period.to(),
                totalRevenue,
                completedOrders,
                averageTicket,
                unitsSold == null ? 0 : unitsSold,
                pendingOrders,
                pendingAmount,
                cancelledOrders,
                buildStatusBreakdown(statusTotals),
                buildDailySales(period, saleStatusCodes),
                orderRepository.findTopProducts(
                                period.start(),
                                period.end(),
                                saleStatusCodes,
                                PageRequest.of(0, TOP_PRODUCTS_LIMIT)
                        ).stream()
                        .map(product -> new SalesSummaryResponse.TopProduct(
                                product.productId(),
                                product.productName(),
                                product.unitsSold() == null ? 0 : product.unitsSold(),
                                product.revenue() == null ? BigDecimal.ZERO : product.revenue()
                        ))
                        .toList()
        );
    }

    // --- Private helpers ---
    private List<SalesSummaryResponse.StatusTotal> buildStatusBreakdown(List<OrderStatusTotalView> statusTotals) {
        Map<String, OrderStatusTotalView> totalsByCode = statusTotals.stream()
                .collect(Collectors.toMap(OrderStatusTotalView::code, Function.identity()));

        return getStatuses().stream()
                .map(status -> {
                    OrderStatusTotalView total = totalsByCode.get(status.code());
                    return new SalesSummaryResponse.StatusTotal(
                            status.code(),
                            status.name(),
                            total == null || total.orderCount() == null ? 0 : total.orderCount(),
                            total == null || total.total() == null ? BigDecimal.ZERO : total.total()
                    );
                })
                .toList();
    }

    private List<SalesSummaryResponse.DailySale> buildDailySales(SalesPeriod period, List<String> saleStatusCodes) {
        Map<LocalDate, SalesSummaryResponse.DailySale> salesByDate = orderRepository.sumDailySales(
                        period.start(),
                        period.end(),
                        saleStatusCodes,
                        SalesPeriod.STORE_ZONE.getId()
                ).stream()
                .map(row -> new SalesSummaryResponse.DailySale(
                        toLocalDate(row[0]),
                        ((Number) row[1]).longValue(),
                        row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString())
                ))
                .collect(Collectors.toMap(SalesSummaryResponse.DailySale::date, Function.identity()));

        return period.from().datesUntil(period.to().plusDays(1))
                .map(date -> salesByDate.getOrDefault(
                        date,
                        new SalesSummaryResponse.DailySale(date, 0, BigDecimal.ZERO)
                ))
                .sorted(Comparator.comparing(SalesSummaryResponse.DailySale::date))
                .toList();
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private SalesPeriod resolvePeriod(LocalDate from, LocalDate to) {
        try {
            return SalesPeriod.of(from, to);
        } catch (IllegalArgumentException exception) {
            throw invalidPeriod();
        }
    }

    private SalesPeriod resolveSummaryPeriod(LocalDate from, LocalDate to) {
        SalesPeriod period;
        try {
            period = SalesPeriod.lastDays(from, to, SUMMARY_DEFAULT_DAYS);
        } catch (IllegalArgumentException exception) {
            throw invalidPeriod();
        }
        if (period.from().plusYears(1).isBefore(period.to())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rango del resumen no puede superar un año."
            );
        }
        return period;
    }

    private static ResponseStatusException invalidPeriod() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha inicial no puede ser posterior a la fecha final."
        );
    }

    private Order requireOrder(UUID orderId) {
        return orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El pedido no existe."
                ));
    }

    private OrderStatus requireStatus(OrderStatusCode code) {
        return orderStatusRepository.findById(code.name())
                .orElseThrow(() -> new IllegalStateException("Missing order status: " + code));
    }
}
