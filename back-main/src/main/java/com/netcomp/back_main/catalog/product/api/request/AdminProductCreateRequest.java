package com.netcomp.back_main.catalog.product.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AdminProductCreateRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String name,

        @Size(max = 150, message = "El slug no puede superar 150 caracteres")
        String slug,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal price,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "El estado activo es obligatorio")
        Boolean active,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100, message = "La categoría no puede superar 100 caracteres")
        String categorySlug,

        @NotNull(message = "La marca es obligatoria")
        UUID brandId,

        @Valid
        List<AdminProductImageCreateRequest> images,

        Map<String, Object> specifications
) {
}
