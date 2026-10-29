package com.netcomp.back_main.catalog.category.repository;

import com.netcomp.back_main.catalog.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    Optional<Category> findBySlugAndActiveTrue(String slug);
    boolean existsBySlug(String slug);
    List<Category> findAllByActiveTrueOrderByNameAsc();
}
