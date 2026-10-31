package com.netcomp.back_main.catalog.product.api.controller;

import com.netcomp.back_main.catalog.product.api.request.AdminProductSearchRequest;
import com.netcomp.back_main.catalog.product.api.request.AdminProductCreateRequest;
import com.netcomp.back_main.catalog.product.api.request.AdminProductUpdateRequest;
import com.netcomp.back_main.catalog.product.api.response.AdminProductEmbeddingResponse;
import com.netcomp.back_main.catalog.product.api.response.AdminProductDetailResponse;
import com.netcomp.back_main.catalog.product.api.response.AdminProductResponse;
import com.netcomp.back_main.catalog.product.service.AdminProductService;
import com.netcomp.back_main.common.api.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final AdminProductService productService;

    public AdminProductController(AdminProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminProductResponse>> list(@Valid @ModelAttribute AdminProductSearchRequest request) {
        return ResponseEntity.ok(productService.getProducts(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductDetailResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminProductDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AdminProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PostMapping
    public ResponseEntity<AdminProductResponse> create(@Valid @RequestBody AdminProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PostMapping("/embeddings")
    public ResponseEntity<AdminProductEmbeddingResponse> generateEmbeddings() {
        return ResponseEntity.ok(productService.generateMissingEmbeddings());
    }
}
