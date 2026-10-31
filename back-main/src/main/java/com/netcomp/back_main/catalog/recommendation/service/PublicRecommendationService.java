package com.netcomp.back_main.catalog.recommendation.service;

import com.netcomp.back_main.catalog.product.api.response.ProductResponse;
import com.netcomp.back_main.catalog.recommendation.repository.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublicRecommendationService {

    // Constants
    private static final int RECOMMENDATION_LIMIT = 4;

    // Dependencies
    private final RecommendationRepository recommendationRepository;

    // Constructor
    public PublicRecommendationService(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    // Public operations
    public List<ProductResponse> getRandomRecommendations(UUID sourceProductId) {
        return recommendationRepository.findRandomActiveInStockProducts(sourceProductId, RECOMMENDATION_LIMIT)
                .stream()
                .map(ProductResponse::from)
                .toList();
    }

    // Private helpers
}
