package com.netcomp.back_main.config.seeder;

import com.netcomp.back_main.catalog.brand.domain.Brand;
import com.netcomp.back_main.catalog.brand.repository.BrandRepository;
import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductSeeder {

    private static final Logger log = LoggerFactory.getLogger(ProductSeeder.class);

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final JsonMapper jsonMapper;

    public ProductSeeder(BrandRepository brandRepository, CategoryRepository categoryRepository,
                         ProductRepository productRepository, JsonMapper jsonMapper) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void seed() {
        assignSeededProductBrands();

        List<ProductSeedData> productSeeds = readProductSeeds();
        int insertedProducts = seedProducts(productSeeds);
        log.info("[ProductSeeder] Catalog seed completed: {} new products from {} definitions.",
                insertedProducts, productSeeds.size());
    }

    private int seedProducts(List<ProductSeedData> productSeeds) {
        int insertedProducts = 0;
        Map<String, Category> categoriesBySlug = new HashMap<>();

        for (ProductSeedData seed : productSeeds) {
            Category category = categoriesBySlug.computeIfAbsent(seed.categorySlug(), categorySlug ->
                    categoryRepository.findBySlug(categorySlug)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Missing seeded category: " + categorySlug))
            );

            Brand brand = getBrand(seed.brandSlug());
            if (createProductIfNotExists(category, brand, seed)) {
                insertedProducts++;
            }
        }

        return insertedProducts;
    }

    private List<ProductSeedData> readProductSeeds() {
        try (InputStream inputStream = new ClassPathResource("seed/products.json").getInputStream()) {
            return jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<ProductSeedData>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read product seed data from seed/products.json.", exception);
        }
    }

    private void assignSeededProductBrands() {
        assignProductBrand("asus-rog-strix-scar-16", "asus");
        assignProductBrand("nvidia-geforce-rtx-4090-24gb", "nvidia");
        assignProductBrand("net-custom-ryzen-7-rtx-4070", "net-custom");
        assignProductBrand("samsung-odyssey-34-curvo", "samsung");
        assignProductBrand("corsair-dominator-titanium-32gb", "corsair");
        assignProductBrand("samsung-990-pro-nvme-2tb", "samsung");
        assignProductBrand("teclado-mecanico-minimal-pro", "net-pro");
        assignProductBrand("laptop-asus-rog-strix-g16", "asus");
        assignProductBrand("laptop-lenovo-legion-pro-5", "lenovo");
        assignProductBrand("nvidia-geforce-rtx-4070-ti-super", "nvidia");
        assignProductBrand("amd-radeon-rx-7800-xt-16gb", "amd");
        assignProductBrand("amd-ryzen-7-7800x3d", "amd");
        assignProductBrand("intel-core-i9-14900k", "intel");
        assignProductBrand("monitor-gamer-asus-tuf-27-ips-180hz", "asus");
        assignProductBrand("teclado-mecanico-logitech-g-pro-x", "logitech");
        assignProductBrand("mouse-gamer-razer-deathadder-v3-pro", "razer");
    }

    private void assignProductBrand(String productSlug, String brandSlug) {
        productRepository.updateBrandBySlug(productSlug, getBrand(brandSlug).getId());
    }

    private Brand getBrand(String slug) {
        return brandRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalStateException("Missing seeded brand: " + slug));
    }

    private boolean createProductIfNotExists(Category category, Brand brand, ProductSeedData seed) {
        if (productRepository.existsBySlug(seed.slug())) {
            return false;
        }

        Product product = new Product(
                category,
                brand,
                seed.name(),
                seed.price(),
                seed.stock(),
                seed.slug(),
                true,
                seed.embedding(),
                seed.specifications() != null ? new HashMap<>(seed.specifications()) : new HashMap<>()
        );

        if (seed.images() != null) {
            for (int i = 0; i < seed.images().size(); i++) {
                boolean isPrimary = (i == 0);
                product.addImage(new ProductImage(seed.images().get(i), isPrimary, i));
            }
        }

        productRepository.save(product);
        return true;
    }

    private record ProductSeedData(
            String categorySlug,
            String brandSlug,
            String name,
            BigDecimal price,
            Integer stock,
            String slug,
            List<String> images,
            float[] embedding,
            Map<String, Object> specifications
    ) {
    }
}
