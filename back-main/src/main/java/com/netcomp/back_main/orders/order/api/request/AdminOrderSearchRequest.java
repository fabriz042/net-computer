package com.netcomp.back_main.orders.order.api.request;

import com.netcomp.back_main.common.api.request.PageableRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDate;

public record AdminOrderSearchRequest(
        @Pattern(regexp = "(?i)^(asc|desc)$", message = "order debe ser asc o desc")
        String order,

        @Min(value = 1, message = "page debe ser mayor que cero")
        Integer page,

        @BindParam("per_page")
        @Min(value = 1, message = "per_page debe ser mayor que cero")
        Integer perPage,

        @Size(max = 100, message = "search no puede superar 100 caracteres")
        String search,

        @Pattern(
                regexp = "(?i)^(PENDING|PAID|PROCESSING|SHIPPED|DELIVERED|CANCELLED)$",
                message = "status no es válido"
        )
        String status,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) implements PageableRequest {
}
