package com.netcomp.back_main.common.api.request;

/** Provides shared defaults and limits for paginated API requests. */
public interface PageableRequest {

    Integer page();

    Integer perPage();

    default int resolvedPage() {
        return page() == null ? 1 : page();
    }

    default int resolvedPerPage() {
        return resolvePageSize(perPage());
    }

    static int resolvePageSize(Integer value) {
        return value == null ? 12 : Math.min(value, 50);
    }
}
