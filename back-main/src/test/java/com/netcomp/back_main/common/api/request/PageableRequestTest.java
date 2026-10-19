package com.netcomp.back_main.common.api.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableRequestTest {

    record DummyRequest(Integer page, Integer perPage) implements PageableRequest {}

    @Test
    void defaultValuesAreAppliedWhenNull() {
        DummyRequest request = new DummyRequest(null, null);

        assertEquals(1, request.resolvedPage());
        assertEquals(12, request.resolvedPerPage());
    }

    @Test
    void customValuesAreUsedWhenProvided() {
        DummyRequest request = new DummyRequest(3, 25);

        assertEquals(3, request.resolvedPage());
        assertEquals(25, request.resolvedPerPage());
    }

    @Test
    void perPageIsCappedAtMaximum50() {
        DummyRequest request = new DummyRequest(1, 100);

        assertEquals(50, request.resolvedPerPage());
    }
}
