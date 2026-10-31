package com.netcomp.back_main.catalog.recommendation.api.controller;

import com.netcomp.back_main.catalog.product.api.response.ProductResponse;
import com.netcomp.back_main.catalog.recommendation.service.PublicRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/recommendations")
public class PublicRecommendationController {

    private final PublicRecommendationService recommendationService;

    public PublicRecommendationController(PublicRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> listProductRecommendations(@PathVariable UUID productId) {
        return ResponseEntity.ok(
                recommendationService.getRandomRecommendations(productId)
        );
    }
}
