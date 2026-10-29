package com.netcomp.back_main.config.seeder;

import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.domain.CategorySpecificationDefinition;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class CategorySeeder {

    private static final Logger log = LoggerFactory.getLogger(CategorySeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final JsonMapper jsonMapper;

    public CategorySeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                          JsonMapper jsonMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void seed() {
        List<CategorySeedData> categorySeeds = readCategorySeeds();
        List<Category> categories = categorySeeds.stream()
                .map(this::createCategoryIfMissing)
                .toList();

        moveLegacyPeripheralProducts(
                findCategory(categories, "teclados"),
                findCategory(categories, "mouse")
        );

        log.info("[CategorySeeder] Category seed completed: {} definitions processed.", categorySeeds.size());
    }

    private List<CategorySeedData> readCategorySeeds() {
        try (InputStream inputStream = new ClassPathResource("seed/categories.json").getInputStream()) {
            return jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<CategorySeedData>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read category seed data from seed/categories.json.", exception);
        }
    }

    private Category createCategoryIfMissing(CategorySeedData seed) {
        return categoryRepository.findBySlug(seed.slug())
                .map(category -> {
                    if (category.getSpecificationSchema() == null) {
                        category.setSpecificationSchema(seed.specificationSchema());
                        return categoryRepository.save(category);
                    }
                    return category;
                })
                .orElseGet(() -> {
                    Category category = new Category(seed.slug(), seed.name(), seed.active());
                    category.setSpecificationSchema(seed.specificationSchema());
                    return categoryRepository.save(category);
                });
    }

    private Category findCategory(List<Category> categories, String slug) {
        return categories.stream()
                .filter(category -> category.getSlug().equals(slug))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing seeded category: " + slug));
    }

    private void moveLegacyPeripheralProducts(Category keyboards, Category mice) {
        productRepository.updateCategoryBySlug("teclado-mecanico-logitech-g-pro-x", keyboards.getId());
        productRepository.updateCategoryBySlug("mouse-gamer-razer-deathadder-v3-pro", mice.getId());

        categoryRepository.findBySlug("perifericos").ifPresent(category -> {
            category.setActive(false);
            if (category.getProducts().isEmpty()) {
                categoryRepository.delete(category);
            } else {
                categoryRepository.save(category);
            }
        });
    }

    private record CategorySeedData(
            String slug,
            String name,
            Boolean active,
            List<CategorySpecificationDefinition> specificationSchema
    ) {
        private CategorySeedData {
            active = active != null ? active : true;
            specificationSchema = specificationSchema != null ? specificationSchema : List.of();
        }
    }
}
