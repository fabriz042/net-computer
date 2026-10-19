package com.netcomp.back_main.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final ProductSeeder productSeeder;

    public DatabaseSeeder(ProductSeeder productSeeder) {
        this.productSeeder = productSeeder;
    }

    @Override
    public void run(String... args) {
        log.info("==> [DatabaseSeeder] Starting seeders execution...");
        productSeeder.seed();
        log.info("==> [DatabaseSeeder] All seeders executed successfully.");
    }
}
