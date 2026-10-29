package com.netcomp.back_main.analytics.searchquery.service;

import com.netcomp.back_main.analytics.searchquery.api.response.AdminSearchQueryResponse;
import com.netcomp.back_main.analytics.searchquery.domain.SearchQuery;
import com.netcomp.back_main.analytics.searchquery.repository.SearchQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SearchQueryService {

    private final SearchQueryRepository searchQueryRepository;

    public SearchQueryService(SearchQueryRepository searchQueryRepository) {
        this.searchQueryRepository = searchQueryRepository;
    }

    public List<AdminSearchQueryResponse> getMostSearchedTerms() {
        return searchQueryRepository.findTop20ByOrderBySearchCountDescLastSearchedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<float[]> findEmbeddingForSearchTerm(String rawSearchTerm) {
        if (rawSearchTerm == null || rawSearchTerm.isBlank()) {
            return Optional.empty();
        }

        String normalizedQuery = rawSearchTerm.trim().toLowerCase(Locale.ROOT);
        return searchQueryRepository.findById(normalizedQuery)
                .map(SearchQuery::getEmbedding)
                .filter(Objects::nonNull);
    }

    private AdminSearchQueryResponse toResponse(SearchQuery searchQuery) {
        long searchCount = searchQuery.getSearchCount();
        long searchesWithClick = searchQuery.getSearchesWithClick();
        double clickThroughRate = searchCount == 0
                ? 0.0
                : (double) searchesWithClick / searchCount * 100.0;

        return new AdminSearchQueryResponse(
                searchQuery.getSearchTerm(),
                searchCount,
                searchesWithClick,
                searchQuery.getProductsFound(),
                clickThroughRate,
                searchQuery.getEmbedding() != null
        );
    }
}
