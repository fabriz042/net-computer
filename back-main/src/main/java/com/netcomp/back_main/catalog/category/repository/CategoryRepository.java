package com.netcomp.back_main.catalog.category.repository;

import com.netcomp.back_main.catalog.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findBySlugAndActiveTrue(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    List<Category> findAllByActiveTrueOrderByNameAsc();
    List<Category> findAllByOrderByNameAsc();

    @Query("SELECT COUNT(product) FROM Product product WHERE product.category.id = :categoryId")
    long countProductsByCategoryId(@Param("categoryId") UUID categoryId);
}
