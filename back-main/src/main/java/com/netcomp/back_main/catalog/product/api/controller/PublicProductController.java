package com.netcomp.back_main.catalog.product.api.controller;

import com.netcomp.back_main.catalog.product.api.request.PublicProductSearchRequest;
import com.netcomp.back_main.catalog.product.api.response.ProductResponse;
import com.netcomp.back_main.catalog.product.service.PublicProductService;
import com.netcomp.back_main.common.api.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class PublicProductController {

    private final PublicProductService productService;

    public PublicProductController(PublicProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> list(@Valid @ModelAttribute PublicProductSearchRequest request) {
        return ResponseEntity.ok(productService.getActiveProducts(request));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> retrieve(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getActiveProductBySlug(slug));
    }
}
