package com.netcomp.back_main.orders.order.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public record SalesPeriod(LocalDate from, LocalDate to, Instant start, Instant end) {

    public static final ZoneId STORE_ZONE = ZoneId.of("America/Lima");

    private static final Instant OPEN_START = Instant.parse("2000-01-01T00:00:00Z");
    private static final Instant OPEN_END = Instant.parse("2100-01-01T00:00:00Z");

    public static SalesPeriod of(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("The start date must not be after the end date");
        }
        Instant start = from == null ? OPEN_START : from.atStartOfDay(STORE_ZONE).toInstant();
        Instant end = to == null ? OPEN_END : to.plusDays(1).atStartOfDay(STORE_ZONE).toInstant();
        return new SalesPeriod(from, to, start, end);
    }

    public static SalesPeriod lastDays(LocalDate from, LocalDate to, int days) {
        LocalDate resolvedTo = to == null ? LocalDate.now(STORE_ZONE) : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(days - 1L) : from;
        return of(resolvedFrom, resolvedTo);
    }

    public boolean isOpen() {
        return from == null && to == null;
    }
}
