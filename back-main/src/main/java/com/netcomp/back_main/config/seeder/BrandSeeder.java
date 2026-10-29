package com.netcomp.back_main.config.seeder;

import com.netcomp.back_main.catalog.brand.domain.Brand;
import com.netcomp.back_main.catalog.brand.repository.BrandRepository;
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
public class BrandSeeder {

    private static final Logger log = LoggerFactory.getLogger(BrandSeeder.class);

    private final BrandRepository brandRepository;
    private final JsonMapper jsonMapper;

    public BrandSeeder(BrandRepository brandRepository, JsonMapper jsonMapper) {
        this.brandRepository = brandRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void seed() {
        List<BrandSeedData> brandSeeds = readBrandSeeds();
        int insertedBrands = 0;

        for (BrandSeedData seed : brandSeeds) {
            if (brandRepository.findBySlug(seed.slug()).isEmpty()) {
                brandRepository.save(new Brand(seed.slug(), seed.name()));
                insertedBrands++;
            }
        }

        log.info("[BrandSeeder] Brand seed completed: {} new brands from {} definitions.",
                insertedBrands, brandSeeds.size());
    }

    private List<BrandSeedData> readBrandSeeds() {
        try (InputStream inputStream = new ClassPathResource("seed/brands.json").getInputStream()) {
            return jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<BrandSeedData>>() {
                    }
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read brand seed data from seed/brands.json.", exception);
        }
    }

    private record BrandSeedData(String slug, String name) {
    }
}
