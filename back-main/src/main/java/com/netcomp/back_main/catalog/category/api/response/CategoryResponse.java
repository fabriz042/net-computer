package com.netcomp.back_main.catalog.category.api.response;

import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String slug,
        String name,
        Boolean active,
        List<CategorySpecificationDefinition> specificationSchema
) {
    public static CategoryResponse from(Category category) {
        List<CategorySpecificationDefinition> specificationSchema =
                category.getSpecificationSchema() != null
                        ? List.copyOf(category.getSpecificationSchema())
                        : List.of();

        return new CategoryResponse(
                category.getId(),
                category.getSlug(),
                category.getName(),
                category.getActive(),
                specificationSchema
        );
    }
}
