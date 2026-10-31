package com.netcomp.back_main.users.cart.api.response;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;
import com.netcomp.back_main.users.cart.domain.CartItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        String categorySlug,
        String brandName,
        String imageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        Integer availableStock,
        BigDecimal subtotal,
        boolean available,
        Instant addedAt
) {
    public static CartItemResponse from(CartItem item) {
        Product product = item.getProduct();
        BigDecimal unitPrice = product.getPrice();
        boolean available = Boolean.TRUE.equals(product.getActive())
                && product.getStock() >= item.getQuantity();

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                resolveImageUrl(product.getImages()),
                unitPrice,
                item.getQuantity(),
                product.getStock(),
                unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())),
                available,
                item.getCreatedAt()
        );
    }

    private static String resolveImageUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
    }
}
