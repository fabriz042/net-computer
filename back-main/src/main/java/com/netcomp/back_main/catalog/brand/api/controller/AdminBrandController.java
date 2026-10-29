package com.netcomp.back_main.catalog.brand.api.controller;

import com.netcomp.back_main.catalog.brand.api.response.BrandResponse;
import com.netcomp.back_main.catalog.brand.service.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/brands")
public class AdminBrandController {

    private final BrandService brandService;

    public AdminBrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> list() {
        return ResponseEntity.ok(brandService.getBrands());
    }
}
