package com.netcomp.back_main.common.api.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void mapsPageCorrectlyIntoPageResponse() {
        List<String> items = List.of("Laptop", "Monitor", "Keyboard");
        PageImpl<String> springPage = new PageImpl<>(items, PageRequest.of(0, 12), 25);

        PageResponse<String> response = PageResponse.from(springPage);

        assertNotNull(response);
        assertEquals(3, response.data().size());
        assertEquals("Laptop", response.data().get(0));

        PageResponse.Meta meta = response.meta();
        assertEquals(25, meta.totalItems());
        assertEquals(1, meta.currentPage());
        assertEquals(12, meta.perPage());
        assertEquals(3, meta.totalPages());
        assertTrue(meta.hasNext());
        assertFalse(meta.hasPrevious());
    }

    @Test
    void mapsElementsWithCustomFunction() {
        List<Integer> numbers = List.of(1, 2, 3);
        PageImpl<Integer> springPage = new PageImpl<>(numbers, PageRequest.of(1, 3), 6);

        PageResponse<String> response = PageResponse.from(springPage, n -> "Item #" + n);

        assertEquals(List.of("Item #1", "Item #2", "Item #3"), response.data());
        assertEquals(2, response.meta().currentPage());
        assertFalse(response.meta().hasNext());
        assertTrue(response.meta().hasPrevious());
    }
}
