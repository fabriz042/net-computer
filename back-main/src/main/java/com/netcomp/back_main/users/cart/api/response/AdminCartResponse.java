package com.netcomp.back_main.users.cart.api.response;

import com.netcomp.back_main.users.cart.repository.CartSummaryView;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminCartResponse(
        UUID userId,
        String userName,
        String userEmail,
        long lineCount,
        long itemCount,
        BigDecimal total,
        Instant createdAt,
        Instant lastActivityAt
) {
    public static AdminCartResponse from(CartSummaryView view) {
        return new AdminCartResponse(
                view.userId(),
                view.userName(),
                view.userEmail(),
                view.lineCount() == null ? 0 : view.lineCount(),
                view.itemCount() == null ? 0 : view.itemCount(),
                view.total() == null ? BigDecimal.ZERO : view.total(),
                view.createdAt(),
                view.lastActivityAt() == null ? view.createdAt() : view.lastActivityAt()
        );
    }
}
