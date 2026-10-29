package com.netcomp.back_main.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final BrandSeeder brandSeeder;
    private final CategorySeeder categorySeeder;
    private final ProductSeeder productSeeder;
    private final SearchQuerySeeder searchQuerySeeder;

    public DatabaseSeeder(BrandSeeder brandSeeder, CategorySeeder categorySeeder, ProductSeeder productSeeder,
                          SearchQuerySeeder searchQuerySeeder) {
        this.brandSeeder = brandSeeder;
        this.categorySeeder = categorySeeder;
        this.productSeeder = productSeeder;
        this.searchQuerySeeder = searchQuerySeeder;
    }

    @Override
    public void run(String... args) {
        log.info("==> [DatabaseSeeder] Starting seeders execution...");
        brandSeeder.seed();
        categorySeeder.seed();
        productSeeder.seed();
        searchQuerySeeder.seed();
        log.info("==> [DatabaseSeeder] All seeders executed successfully.");
    }
}
