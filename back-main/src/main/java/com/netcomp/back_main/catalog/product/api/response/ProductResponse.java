package com.netcomp.back_main.catalog.product.api.response;

import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProductResponse(
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
    Instant createdAt
) {
    public static ProductResponse from(Product product) {
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(ProductImage::getImageUrl).toList()
                : List.of();

        String primary = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primary = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(imageUrls.get(0));
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getSlug() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                primary,
                imageUrls,
                product.getSpecifications(),
                product.getCreatedAt()
        );
    }
}
