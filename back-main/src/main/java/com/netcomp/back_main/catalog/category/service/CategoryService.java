package com.netcomp.back_main.catalog.category.service;

import com.netcomp.back_main.catalog.category.api.response.CategoryResponse;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
