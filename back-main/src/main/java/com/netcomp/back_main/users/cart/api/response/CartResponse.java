package com.netcomp.back_main.users.cart.api.response;

import com.netcomp.back_main.users.cart.domain.Cart;
import com.netcomp.back_main.users.cart.domain.CartItem;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID userId,
        List<CartItemResponse> items,
        int itemCount,
        BigDecimal total,
        boolean checkoutAvailable
) {
    public static CartResponse empty(UUID userId) {
        return new CartResponse(userId, List.of(), 0, BigDecimal.ZERO, false);
    }

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .sorted(Comparator.comparing(
                        CartItem::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(CartItemResponse::from)
                .toList();
        int itemCount = items.stream().mapToInt(CartItemResponse::quantity).sum();
        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean checkoutAvailable = !items.isEmpty()
                && items.stream().allMatch(CartItemResponse::available);

        return new CartResponse(cart.getUserId(), items, itemCount, total, checkoutAvailable);
    }
}
