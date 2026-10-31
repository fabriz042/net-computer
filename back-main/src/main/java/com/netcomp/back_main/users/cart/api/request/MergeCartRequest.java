package com.netcomp.back_main.users.cart.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MergeCartRequest(
        @NotNull(message = "Los productos son obligatorios")
        @Size(max = 50, message = "No se pueden combinar más de 50 productos")
        List<@Valid AddToCartRequest> items
) {
}
