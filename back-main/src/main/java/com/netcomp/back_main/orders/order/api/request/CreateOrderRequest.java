package com.netcomp.back_main.orders.order.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotBlank(message = "La dirección de envío es obligatoria")
        @Size(min = 10, max = 255, message = "La dirección de envío debe tener entre 10 y 255 caracteres")
        String shippingAddress
) {
}
