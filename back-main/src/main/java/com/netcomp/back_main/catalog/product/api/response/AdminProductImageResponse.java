package com.netcomp.back_main.catalog.product.api.response;

import com.netcomp.back_main.catalog.product.domain.ProductImage;

public record AdminProductImageResponse(
        String imageUrl,
        boolean isPrimary,
        Integer displayOrder
) {
    public static AdminProductImageResponse from(ProductImage image) {
        return new AdminProductImageResponse(
                image.getImageUrl(),
                Boolean.TRUE.equals(image.getIsPrimary()),
                image.getDisplayOrder()
        );
    }
}
