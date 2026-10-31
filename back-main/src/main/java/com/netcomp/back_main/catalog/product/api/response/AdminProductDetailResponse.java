package com.netcomp.back_main.catalog.product.api.response;

import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminProductDetailResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal price,
        Integer stock,
        Boolean active,
        UUID brandId,
        String brandName,
        UUID categoryId,
        String categoryName,
        String categorySlug,
        Boolean categoryActive,
        List<CategorySpecificationDefinition> specificationSchema,
        List<AdminProductImageResponse> images,
        Map<String, Object> specifications
) {
    public static AdminProductDetailResponse from(Product product) {
        List<AdminProductImageResponse> images = product.getImages() == null
                ? List.of()
                : product.getImages().stream()
                        .map(AdminProductImageResponse::from)
                        .toList();

        return new AdminProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getStock(),
                product.getActive(),
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getSlug(),
                product.getCategory().getActive(),
                product.getCategory().getSpecificationSchema() == null
                        ? List.of()
                        : product.getCategory().getSpecificationSchema(),
                images,
                product.getSpecifications()
        );
    }
}
