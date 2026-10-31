package com.netcomp.back_main.orders.order.domain;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public enum OrderStatusCode {
    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Set<OrderStatusCode> SALE_STATUSES = EnumSet.of(PAID, PROCESSING, SHIPPED, DELIVERED);

    public List<OrderStatusCode> nextStatuses() {
        return switch (this) {
            case PENDING -> List.of(PAID, CANCELLED);
            case PAID -> List.of(PROCESSING, CANCELLED);
            case PROCESSING -> List.of(SHIPPED, CANCELLED);
            case SHIPPED -> List.of(DELIVERED);
            case DELIVERED, CANCELLED -> List.of();
        };
    }

    public boolean canTransitionTo(OrderStatusCode target) {
        return nextStatuses().contains(target);
    }

    public boolean countsAsSale() {
        return SALE_STATUSES.contains(this);
    }

    public static Set<OrderStatusCode> saleStatuses() {
        return EnumSet.copyOf(SALE_STATUSES);
    }

    public static OrderStatusCode from(String code) {
        try {
            return OrderStatusCode.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Unknown order status: " + code, exception);
        }
    }
}
