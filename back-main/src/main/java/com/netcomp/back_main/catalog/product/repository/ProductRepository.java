package com.netcomp.back_main.catalog.product.repository;

import com.netcomp.back_main.catalog.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query(value = "SELECT * FROM products WHERE slug = :slug", nativeQuery = true)
    Optional<Product> findBySlug(@Param("slug") String slug);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM products WHERE slug = :slug)", nativeQuery = true)
    boolean existsBySlug(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"category", "images", "brand"})
    @Query(value = "SELECT * FROM products WHERE slug = :slug AND active = TRUE", nativeQuery = true)
    Optional<Product> findBySlugAndActiveTrue(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"category", "brand"})
    @Query(value = "SELECT * FROM products WHERE embedding IS NULL ORDER BY created_at ASC", nativeQuery = true)
    List<Product> findAllByEmbeddingIsNullOrderByCreatedAtAsc();

    @EntityGraph(attributePaths = {"category", "images", "brand"})
    @Query(value = "SELECT * FROM products WHERE id IN (:ids)", nativeQuery = true)
    List<Product> findAllByIdIn(@Param("ids") List<UUID> ids);

    @Modifying
    @Query(value = "UPDATE products SET category_id = :categoryId " +
            "WHERE slug = :slug AND category_id <> :categoryId", nativeQuery = true)
    int updateCategoryBySlug(@Param("slug") String slug, @Param("categoryId") UUID categoryId);

    @Modifying
    @Query(value = "UPDATE products SET brand_id = :brandId " +
            "WHERE slug = :slug AND (brand_id IS NULL OR brand_id <> :brandId)", nativeQuery = true)
    int updateBrandBySlug(@Param("slug") String slug, @Param("brandId") UUID brandId);

    @Query(value = """
            SELECT product.*
            FROM products product
            JOIN categories category ON category.id = product.category_id
            LEFT JOIN brands brand ON brand.id = product.brand_id
            WHERE (:categorySlug IS NULL OR category.slug = :categorySlug)
                AND (
                    :search = ''
                    OR product.name ILIKE CONCAT('%', :search, '%')
                    OR product.slug ILIKE CONCAT('%', :search, '%')
                    OR category.name ILIKE CONCAT('%', :search, '%')
                    OR category.slug ILIKE CONCAT('%', :search, '%')
                    OR brand.name ILIKE CONCAT('%', :search, '%')
                )
            ORDER BY
                CASE WHEN :sortOrder = 'asc' THEN product.created_at END ASC,
                CASE WHEN :sortOrder = 'desc' THEN product.created_at END DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products product
            JOIN categories category ON category.id = product.category_id
            LEFT JOIN brands brand ON brand.id = product.brand_id
            WHERE (:categorySlug IS NULL OR category.slug = :categorySlug)
                AND :sortOrder IN ('asc', 'desc')
                AND (
                    :search = ''
                    OR product.name ILIKE CONCAT('%', :search, '%')
                    OR product.slug ILIKE CONCAT('%', :search, '%')
                    OR category.name ILIKE CONCAT('%', :search, '%')
                    OR category.slug ILIKE CONCAT('%', :search, '%')
                    OR brand.name ILIKE CONCAT('%', :search, '%')
                )
            """,
            nativeQuery = true)
    Page<Product> searchAdminProducts(
            @Param("search") String search,
            @Param("categorySlug") String categorySlug,
            @Param("sortOrder") String sortOrder,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "images", "brand"})
    @Query(value = "SELECT * FROM products WHERE active = TRUE",
            countQuery = "SELECT COUNT(*) FROM products WHERE active = TRUE",
            nativeQuery = true)
    Page<Product> findByActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "images", "brand"})
    @Query(value = """
            SELECT product.*
            FROM products product
            JOIN categories category ON category.id = product.category_id
            LEFT JOIN brands brand ON brand.id = product.brand_id
            WHERE product.active = TRUE
                AND (:categorySlug IS NULL OR category.slug = :categorySlug)
                AND (
                    :search = ''
                    OR product.name ILIKE CONCAT('%', :search, '%')
                    OR product.slug ILIKE CONCAT('%', :search, '%')
                    OR category.name ILIKE CONCAT('%', :search, '%')
                    OR category.slug ILIKE CONCAT('%', :search, '%')
                    OR brand.name ILIKE CONCAT('%', :search, '%')
                )
            ORDER BY
                CASE WHEN :sortOrder = 'asc' THEN product.created_at END ASC,
                CASE WHEN :sortOrder = 'desc' THEN product.created_at END DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products product
            JOIN categories category ON category.id = product.category_id
            LEFT JOIN brands brand ON brand.id = product.brand_id
            WHERE product.active = TRUE
                AND (:categorySlug IS NULL OR category.slug = :categorySlug)
                AND :sortOrder IN ('asc', 'desc')
                AND (
                    :search = ''
                    OR product.name ILIKE CONCAT('%', :search, '%')
                    OR product.slug ILIKE CONCAT('%', :search, '%')
                    OR category.name ILIKE CONCAT('%', :search, '%')
                    OR category.slug ILIKE CONCAT('%', :search, '%')
                    OR brand.name ILIKE CONCAT('%', :search, '%')
                )
            """,
            nativeQuery = true)
    Page<Product> searchActiveProducts(
            @Param("search") String search,
            @Param("categorySlug") String categorySlug,
            @Param("sortOrder") String sortOrder,
            Pageable pageable
    );

    @Query(value = """
            SELECT product.id
            FROM products product
            JOIN categories category ON category.id = product.category_id
            WHERE product.active = TRUE
                AND product.embedding IS NOT NULL
                AND (:categorySlug IS NULL OR category.slug = :categorySlug)
                AND product.embedding <=> CAST(:queryEmbedding AS vector(1024)) <= :maxDistance
            ORDER BY product.embedding <=> CAST(:queryEmbedding AS vector(1024)) ASC,
                     product.created_at DESC
            """,
            nativeQuery = true)
    List<UUID> searchActiveProductIdsByEmbedding(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("categorySlug") String categorySlug,
            @Param("maxDistance") double maxDistance,
            Pageable pageable
    );
}
