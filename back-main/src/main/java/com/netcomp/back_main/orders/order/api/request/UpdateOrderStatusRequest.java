package com.netcomp.back_main.orders.order.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "^(PENDING|PAID|PROCESSING|SHIPPED|DELIVERED|CANCELLED)$",
                message = "El estado no es válido"
        )
        String status
) {
}
