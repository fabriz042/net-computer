package com.netcomp.back_main.analytics.searchquery.api.response;

public record AdminSearchQueryResponse(
        String searchTerm,
        long searchCount,
        long searchesWithClick,
        long productsFound,
        double clickThroughRate,
        boolean hasEmbedding
) {
}
