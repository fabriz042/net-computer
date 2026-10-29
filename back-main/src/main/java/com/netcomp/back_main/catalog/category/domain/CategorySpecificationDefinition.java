package com.netcomp.back_main.catalog.category.domain;

import java.util.List;

public record CategorySpecificationDefinition(
        String key,
        String label,
        String type,
        boolean required,
        String unit,
        List<String> options
) {
    public CategorySpecificationDefinition {
        options = options == null ? List.of() : List.copyOf(options);
    }
}
