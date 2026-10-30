package com.netcomp.back_main.catalog.category.api.request;

import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String name,

        @Size(max = 100, message = "El slug no puede superar 100 caracteres")
        String slug,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean active,

        List<CategorySpecificationDefinition> specificationSchema
) {
}
