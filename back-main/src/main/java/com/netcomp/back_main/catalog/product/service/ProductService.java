package com.netcomp.back_main.catalog.product.service;

import com.netcomp.back_main.catalog.product.api.request.PublicProductSearchRequest;
import com.netcomp.back_main.catalog.product.api.response.ProductResponse;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import com.netcomp.back_main.common.api.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public PageResponse<ProductResponse> getActiveProducts(PublicProductSearchRequest request) {
        PublicProductSearchRequest searchRequest = request != null ? request : new PublicProductSearchRequest(null, null, null);
        Sort.Direction direction = "asc".equalsIgnoreCase(searchRequest.order()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                searchRequest.resolvedPage() - 1,
                searchRequest.resolvedPerPage(),
                Sort.by(direction, "createdAt")
        );

        Page<Product> page = productRepository.findByActiveTrue(pageable);
        return PageResponse.from(page, ProductResponse::from);
    }
}
