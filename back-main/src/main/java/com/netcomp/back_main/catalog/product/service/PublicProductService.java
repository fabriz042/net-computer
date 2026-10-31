package com.netcomp.back_main.catalog.product.service;

import com.netcomp.back_main.analytics.searchquery.service.SearchQueryService;
import com.netcomp.back_main.catalog.product.api.request.PublicProductSearchRequest;
import com.netcomp.back_main.catalog.product.api.response.ProductResponse;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import com.netcomp.back_main.common.api.response.PageResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PublicProductService {

    // --- Constants ---
    private static final double MAX_COSINE_DISTANCE = 0.70;
    private static final int MAX_VECTOR_SEARCH_RESULTS = 4;

    // --- Dependencies ---
    private final ProductRepository productRepository;
    private final SearchQueryService searchQueryService;

    // --- Constructor ---
    public PublicProductService(
        ProductRepository productRepository,
        SearchQueryService searchQueryService
    ) {
        this.productRepository = productRepository;
        this.searchQueryService = searchQueryService;
    }

    // --- Public operations ---
    public PageResponse<ProductResponse> getActiveProducts(PublicProductSearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.resolvedPage() - 1,
                request.resolvedPerPage()
        );
        String sortOrder = "asc".equals(request.order()) ? "asc" : "desc";
        String search = StringUtils.trimToEmpty(request.search());
        String categorySlug = StringUtils.isBlank(request.categorySlug())
                ? null
                : StringUtils.trim(request.categorySlug());
        Page<Product> page = searchActiveProducts(search, categorySlug, sortOrder, pageable);
        PageResponse<ProductResponse> response = PageResponse.from(page, ProductResponse::from);

        return response;
    }

    public ProductResponse getActiveProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado."
                ));
        ProductResponse response = ProductResponse.from(product);

        return response;
    }

    // --- Private helpers ---
    private Page<Product> searchActiveProducts(String search, String categorySlug, String sortOrder, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return productRepository.searchActiveProducts("", categorySlug, sortOrder, pageable);
        }

        float[] queryEmbedding = searchQueryService.findEmbeddingForSearchTerm(search).orElse(null);
        if (queryEmbedding == null || queryEmbedding.length != 1024) {
            return productRepository.searchActiveProducts(search, categorySlug, sortOrder, pageable);
        }

        return searchActiveProductsByEmbedding(queryEmbedding, categorySlug, pageable);
    }

    private Page<Product> searchActiveProductsByEmbedding( float[] queryEmbedding, String categorySlug, Pageable pageable) {
        Pageable vectorPageable = PageRequest.of(0, MAX_VECTOR_SEARCH_RESULTS);
        List<UUID> nearestProductIds = productRepository.searchActiveProductIdsByEmbedding(
                toVectorLiteral(queryEmbedding),
                categorySlug,
                MAX_COSINE_DISTANCE,
                vectorPageable
        );

        int fromIndex = (int) Math.min(pageable.getOffset(), nearestProductIds.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), nearestProductIds.size());
        List<UUID> productIds = nearestProductIds.subList(fromIndex, toIndex);
        if (productIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, nearestProductIds.size());
        }

        Map<UUID, Product> productsById = productRepository.findAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        List<Product> orderedProducts = productIds.stream()
                .map(productsById::get)
                .filter(product -> product != null)
                .toList();

        return new PageImpl<>(orderedProducts, pageable, nearestProductIds.size());
    }

    private String toVectorLiteral(float[] vector) {
        return Arrays.toString(vector);
    }
}
