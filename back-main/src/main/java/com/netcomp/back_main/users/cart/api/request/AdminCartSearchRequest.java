package com.netcomp.back_main.users.cart.api.request;

import com.netcomp.back_main.common.api.request.PageableRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.BindParam;

public record AdminCartSearchRequest(
        @Min(value = 1, message = "page debe ser mayor que cero")
        Integer page,

        @BindParam("per_page")
        @Min(value = 1, message = "per_page debe ser mayor que cero")
        Integer perPage,

        @Size(max = 100, message = "search no puede superar 100 caracteres")
        String search
) implements PageableRequest {
}
