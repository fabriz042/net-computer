package com.netcomp.back_main.catalog.brand.repository;

import com.netcomp.back_main.catalog.brand.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    List<Brand> findAllByOrderByNameAsc();

    @Query("SELECT COUNT(product) FROM Product product WHERE product.brand.id = :brandId")
    long countProductsByBrandId(@Param("brandId") UUID brandId);
}
