package com.netcomp.back_main.analytics.searchquery.service;

import com.netcomp.back_main.analytics.searchquery.api.response.AdminSearchQueryEmbeddingResponse;
import com.netcomp.back_main.analytics.searchquery.domain.SearchQuery;
import com.netcomp.back_main.analytics.searchquery.repository.SearchQueryRepository;
import com.netcomp.back_main.common.embedding.BedrockEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminSearchQueryEmbeddingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSearchQueryEmbeddingService.class);
    private static final String INPUT_TYPE = "search_query";
    private static final int EMBEDDING_DIMENSION = 1024;

    private final SearchQueryRepository searchQueryRepository;
    private final BedrockEmbeddingService bedrockEmbeddingService;

    public AdminSearchQueryEmbeddingService(SearchQueryRepository searchQueryRepository,
                                            BedrockEmbeddingService bedrockEmbeddingService) {
        this.searchQueryRepository = searchQueryRepository;
        this.bedrockEmbeddingService = bedrockEmbeddingService;
    }

    public AdminSearchQueryEmbeddingResponse generateTopMissingEmbeddings() {
        List<SearchQuery> searchQueries = searchQueryRepository
                .findTop20ByEmbeddingIsNullOrderBySearchCountDescLastSearchedAtDesc();
        if (searchQueries.isEmpty()) {
            return new AdminSearchQueryEmbeddingResponse(0);
        }

        List<List<Float>> embeddings;
        try {
            embeddings = bedrockEmbeddingService.generateEmbeddings(
                    searchQueries.stream().map(SearchQuery::getSearchTerm).toList(),
                    INPUT_TYPE
            );
        } catch (Exception exception) {
            LOGGER.error("Failed to generate frequent search embeddings with Amazon Bedrock.", exception);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "No se pudieron generar los embeddings de las búsquedas.",
                    exception
            );
        }

        if (embeddings == null || embeddings.size() != searchQueries.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio de embeddings devolvió una cantidad inesperada de resultados."
            );
        }

        for (int index = 0; index < searchQueries.size(); index++) {
            searchQueries.get(index).setEmbedding(toVector(embeddings.get(index)));
        }

        searchQueryRepository.saveAll(searchQueries);
        return new AdminSearchQueryEmbeddingResponse(searchQueries.size());
    }

    private float[] toVector(List<Float> values) {
        if (values == null || values.size() != EMBEDDING_DIMENSION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "El servicio de embeddings devolvió un vector con una dimensión inesperada."
            );
        }

        float[] vector = new float[EMBEDDING_DIMENSION];
        for (int index = 0; index < values.size(); index++) {
            vector[index] = values.get(index);
        }
        return vector;
    }
}
