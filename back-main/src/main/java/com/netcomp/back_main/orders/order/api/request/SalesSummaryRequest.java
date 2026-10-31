package com.netcomp.back_main.orders.order.api.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SalesSummaryRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {
}
