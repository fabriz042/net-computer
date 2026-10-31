package com.netcomp.back_main.orders.order.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesPeriodTest {

    @Test
    void convertsStoreDatesToInclusiveUtcRange() {
        SalesPeriod period = SalesPeriod.of(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));

        assertEquals(Instant.parse("2026-10-01T05:00:00Z"), period.start());
        assertEquals(Instant.parse("2026-11-01T05:00:00Z"), period.end());
        assertFalse(period.isOpen());
    }

    @Test
    void usesOpenBoundsWhenDatesAreMissing() {
        SalesPeriod period = SalesPeriod.of(null, null);

        assertTrue(period.isOpen());
        assertTrue(period.start().isBefore(Instant.parse("2001-01-01T00:00:00Z")));
        assertTrue(period.end().isAfter(Instant.parse("2099-01-01T00:00:00Z")));
    }

    @Test
    void resolvesDefaultWindowEndingOnGivenDate() {
        SalesPeriod period = SalesPeriod.lastDays(null, LocalDate.of(2026, 10, 30), 30);

        assertEquals(LocalDate.of(2026, 10, 1), period.from());
        assertEquals(LocalDate.of(2026, 10, 30), period.to());
    }

    @Test
    void rejectsInvertedRanges() {
        assertThrows(IllegalArgumentException.class,
                () -> SalesPeriod.of(LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 1)));
    }
}
