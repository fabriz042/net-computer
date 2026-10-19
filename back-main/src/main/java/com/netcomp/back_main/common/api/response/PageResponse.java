package com.netcomp.back_main.common.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(Meta meta, List<T> data) {

    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        Meta meta = new Meta(
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
        return new PageResponse<>(meta, page.getContent().stream().map(mapper).toList());
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return from(page, Function.identity());
    }

    public record Meta(
            @JsonProperty("total_items") long totalItems,
            @JsonProperty("current_page") int currentPage,
            @JsonProperty("per_page") int perPage,
            @JsonProperty("total_pages") int totalPages,
            @JsonProperty("has_next") boolean hasNext,
            @JsonProperty("has_previous") boolean hasPrevious
    ) {}
}
