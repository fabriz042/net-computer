package com.netcomp.back_main.catalog.product.api.request;

import com.netcomp.back_main.common.api.request.PageableRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.BindParam;

public record PublicProductSearchRequest(
        @Pattern(regexp = "(?i)^(asc|desc)$", message = "order debe ser asc o desc")
        String order,

        @Min(value = 1, message = "page debe ser mayor que cero")
        Integer page,

        @BindParam("per_page")
        @Min(value = 1, message = "per_page debe ser mayor que cero")
        Integer perPage
) implements PageableRequest {}
