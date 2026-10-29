package com.netcomp.back_main.catalog.product.api.response;

import com.netcomp.back_main.catalog.product.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminProductResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        Integer stock,
        Boolean active,
        UUID brandId,
        String brandSlug,
        String brandName,
        String categoryName,
        String categorySlug,
        String primaryImageUrl,
        List<String> images,
        Map<String, Object> specifications,
        boolean hasEmbedding,
        Instant createdAt
) {
    public static AdminProductResponse from(Product product) {
        ProductResponse productResponse = ProductResponse.from(product);
        return new AdminProductResponse(
                productResponse.id(),
                productResponse.name(),
                productResponse.slug(),
                productResponse.price(),
                productResponse.stock(),
                productResponse.active(),
                productResponse.brandId(),
                productResponse.brandSlug(),
                productResponse.brandName(),
                productResponse.categoryName(),
                productResponse.categorySlug(),
                productResponse.primaryImageUrl(),
                productResponse.images(),
                productResponse.specifications(),
                product.getEmbedding() != null,
                productResponse.createdAt()
        );
    }
}
