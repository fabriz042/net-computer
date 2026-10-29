package com.netcomp.back_main.catalog.brand.service;

import com.netcomp.back_main.catalog.brand.api.response.BrandResponse;
import com.netcomp.back_main.catalog.brand.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandResponse> getBrands() {
        return brandRepository.findAllByOrderByNameAsc()
                .stream()
                .map(BrandResponse::from)
                .toList();
    }
}
