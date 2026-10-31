package com.netcomp.back_main.orders.order.api.response;

import com.netcomp.back_main.orders.order.domain.Order;
import com.netcomp.back_main.orders.order.domain.OrderStatusCode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String code,
        String status,
        String statusName,
        List<String> nextStatuses,
        BigDecimal total,
        int itemCount,
        String shippingAddress,
        UUID customerId,
        String customerName,
        String customerEmail,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                toCode(order.getId()),
                order.getStatus().getCode(),
                order.getStatus().getName(),
                OrderStatusCode.from(order.getStatus().getCode()).nextStatuses().stream()
                        .map(Enum::name)
                        .toList(),
                order.getTotal(),
                items.stream().mapToInt(OrderItemResponse::quantity).sum(),
                order.getShippingAddress(),
                order.getUser().getId(),
                order.getUser().getName(),
                order.getUser().getEmail(),
                items,
                order.getCreatedAt()
        );
    }

    public static String toCode(UUID orderId) {
        return orderId == null ? null : orderId.toString().substring(0, 8).toUpperCase();
    }
}
