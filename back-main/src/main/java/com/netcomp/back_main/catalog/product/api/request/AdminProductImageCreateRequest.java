package com.netcomp.back_main.catalog.product.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminProductImageCreateRequest(
        @NotBlank(message = "La URL de la imagen es obligatoria")
        @Size(max = 500, message = "La URL no puede superar 500 caracteres")
        String imageUrl,

        Boolean isPrimary,

        @NotNull(message = "El orden de la imagen es obligatorio")
        @Min(value = 0, message = "El orden no puede ser negativo")
        Integer displayOrder
) {
}
