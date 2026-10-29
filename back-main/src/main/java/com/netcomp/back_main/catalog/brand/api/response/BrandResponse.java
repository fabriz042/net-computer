package com.netcomp.back_main.catalog.brand.api.response;

import com.netcomp.back_main.catalog.brand.domain.Brand;

import java.util.UUID;

public record BrandResponse(UUID id, String slug, String name) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getSlug(), brand.getName());
    }
}
