package com.netcomp.back_main.orders.order.repository;

import java.math.BigDecimal;

public record OrderStatusTotalView(String code, String name, Long orderCount, BigDecimal total) {
}
