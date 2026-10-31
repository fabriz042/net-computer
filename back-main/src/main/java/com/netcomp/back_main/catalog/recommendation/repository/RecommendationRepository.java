package com.netcomp.back_main.catalog.recommendation.repository;

import com.netcomp.back_main.catalog.product.domain.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends Repository<Product, UUID> {

    @Query(value = """
            SELECT product.*
            FROM products product
            WHERE product.active = TRUE
                AND product.stock > 0
                AND product.id <> :sourceProductId
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<Product> findRandomActiveInStockProducts(
            @Param("sourceProductId") UUID sourceProductId,
            @Param("limit") int limit
    );
}
