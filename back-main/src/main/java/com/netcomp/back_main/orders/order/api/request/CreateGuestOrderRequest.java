package com.netcomp.back_main.orders.order.api.request;

import com.netcomp.back_main.users.cart.api.request.AddToCartRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateGuestOrderRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String customerName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico no es válido")
        @Size(max = 100, message = "El correo electrónico no puede superar los 100 caracteres")
        String customerEmail,

        @NotBlank(message = "La dirección de envío es obligatoria")
        @Size(min = 10, max = 255, message = "La dirección de envío debe tener entre 10 y 255 caracteres")
        String shippingAddress,

        @NotEmpty(message = "Tu carrito está vacío.")
        @Size(max = 50, message = "No se pueden comprar más de 50 productos distintos")
        List<@Valid AddToCartRequest> items
) {
}
