package com.netcomp.back_main.users.cart.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CartSummaryView(
        UUID userId,
        String userName,
        String userEmail,
        Long lineCount,
        Long itemCount,
        BigDecimal total,
        Instant createdAt,
        Instant lastActivityAt
) {
}
