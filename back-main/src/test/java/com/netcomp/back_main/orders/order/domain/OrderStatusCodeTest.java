package com.netcomp.back_main.orders.order.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusCodeTest {

    @Test
    void allowsOnlyForwardTransitionsAndCancellationBeforeShipping() {
        assertEquals(List.of(OrderStatusCode.PAID, OrderStatusCode.CANCELLED), OrderStatusCode.PENDING.nextStatuses());
        assertTrue(OrderStatusCode.PAID.canTransitionTo(OrderStatusCode.PROCESSING));
        assertTrue(OrderStatusCode.PROCESSING.canTransitionTo(OrderStatusCode.CANCELLED));
        assertFalse(OrderStatusCode.SHIPPED.canTransitionTo(OrderStatusCode.CANCELLED));
        assertFalse(OrderStatusCode.PENDING.canTransitionTo(OrderStatusCode.DELIVERED));
        assertTrue(OrderStatusCode.DELIVERED.nextStatuses().isEmpty());
        assertTrue(OrderStatusCode.CANCELLED.nextStatuses().isEmpty());
    }

    @Test
    void countsOnlyPaidOrLaterStatusesAsSales() {
        assertFalse(OrderStatusCode.PENDING.countsAsSale());
        assertFalse(OrderStatusCode.CANCELLED.countsAsSale());
        assertTrue(OrderStatusCode.PAID.countsAsSale());
        assertTrue(OrderStatusCode.DELIVERED.countsAsSale());
    }

    @Test
    void parsesCodesIgnoringCaseAndRejectsUnknownValues() {
        assertEquals(OrderStatusCode.SHIPPED, OrderStatusCode.from(" shipped "));
        assertThrows(IllegalArgumentException.class, () -> OrderStatusCode.from("LOST"));
        assertThrows(IllegalArgumentException.class, () -> OrderStatusCode.from(null));
    }
}
