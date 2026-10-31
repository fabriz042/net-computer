package com.netcomp.back_main.orders.order.repository;

import java.math.BigDecimal;
import java.util.UUID;

public record TopProductView(UUID productId, String productName, Long unitsSold, BigDecimal revenue) {
}
