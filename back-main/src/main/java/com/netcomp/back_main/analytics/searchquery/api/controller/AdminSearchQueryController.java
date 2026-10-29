package com.netcomp.back_main.analytics.searchquery.api.controller;

import com.netcomp.back_main.analytics.searchquery.api.response.AdminSearchQueryResponse;
import com.netcomp.back_main.analytics.searchquery.api.response.AdminSearchQueryEmbeddingResponse;
import com.netcomp.back_main.analytics.searchquery.service.AdminSearchQueryEmbeddingService;
import com.netcomp.back_main.analytics.searchquery.service.SearchQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/search-queries")
public class AdminSearchQueryController {

    private final SearchQueryService searchQueryService;
    private final AdminSearchQueryEmbeddingService embeddingService;

    public AdminSearchQueryController(SearchQueryService searchQueryService,
                                      AdminSearchQueryEmbeddingService embeddingService) {
        this.searchQueryService = searchQueryService;
        this.embeddingService = embeddingService;
    }

    @GetMapping
    public ResponseEntity<List<AdminSearchQueryResponse>> listMostSearchedTerms() {
        return ResponseEntity.ok(searchQueryService.getMostSearchedTerms());
    }

    @PostMapping("/embeddings")
    public ResponseEntity<AdminSearchQueryEmbeddingResponse> generateMissingEmbeddings() {
        return ResponseEntity.ok(embeddingService.generateTopMissingEmbeddings());
    }
}
