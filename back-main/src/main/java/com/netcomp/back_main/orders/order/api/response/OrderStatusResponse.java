package com.netcomp.back_main.orders.order.api.response;

import com.netcomp.back_main.orders.order.domain.OrderStatus;

public record OrderStatusResponse(String code, String name) {
    public static OrderStatusResponse from(OrderStatus status) {
        return new OrderStatusResponse(status.getCode(), status.getName());
    }
}
