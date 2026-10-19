package com.netcomp.back_main.config.seeder;

import com.netcomp.back_main.catalog.category.domain.Category;
import com.netcomp.back_main.catalog.category.repository.CategoryRepository;
import com.netcomp.back_main.catalog.product.domain.Product;
import com.netcomp.back_main.catalog.product.domain.ProductImage;
import com.netcomp.back_main.catalog.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductSeeder {

    private static final Logger log = LoggerFactory.getLogger(ProductSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void seed() {
        if (categoryRepository.count() > 0 && productRepository.count() > 0) {
            log.info("[ProductSeeder] Catalog data already exists, skipping seed.");
            return;
        }

        log.info("[ProductSeeder] Seeding Net Computer categories and products...");

        // 1. Categories
        Category laptops = getOrCreateCategory("laptops", "Laptops");
        Category gpus = getOrCreateCategory("tarjetas-de-video", "Tarjetas de Video");
        Category cpus = getOrCreateCategory("procesadores", "Procesadores");
        Category monitors = getOrCreateCategory("monitores", "Monitores");
        Category peripherals = getOrCreateCategory("perifericos", "Perifericos");

        // 2. Products
        createProductIfNotExists(
                laptops,
                "Laptop ASUS ROG Strix G16",
                new BigDecimal("1699.99"),
                8,
                "laptop-asus-rog-strix-g16",
                List.of(
                        "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "processor", "Intel Core i7-13650HX",
                        "ram", "16GB DDR5",
                        "storage", "512GB NVMe SSD",
                        "gpu", "NVIDIA GeForce RTX 4060 8GB",
                        "display", "16 FHD+ 165Hz"
                )
        );

        createProductIfNotExists(
                laptops,
                "Laptop Lenovo Legion Pro 5",
                new BigDecimal("1899.00"),
                5,
                "laptop-lenovo-legion-pro-5",
                List.of(
                        "https://images.unsplash.com/photo-1593642632823-8f785ba67e45?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "processor", "AMD Ryzen 7 7745HX",
                        "ram", "32GB DDR5",
                        "storage", "1TB NVMe SSD",
                        "gpu", "NVIDIA GeForce RTX 4070 8GB",
                        "display", "16 WQXGA 240Hz"
                )
        );

        createProductIfNotExists(
                gpus,
                "NVIDIA GeForce RTX 4070 Ti SUPER 16GB",
                new BigDecimal("899.99"),
                12,
                "nvidia-geforce-rtx-4070-ti-super",
                List.of(
                        "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "vram", "16GB GDDR6X",
                        "interface", "PCIe 4.0",
                        "ports", "3x DisplayPort 1.4a, 1x HDMI 2.1a",
                        "recommended_psu", "750W"
                )
        );

        createProductIfNotExists(
                gpus,
                "AMD Radeon RX 7800 XT 16GB",
                new BigDecimal("549.99"),
                10,
                "amd-radeon-rx-7800-xt-16gb",
                List.of(
                        "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "vram", "16GB GDDR6",
                        "interface", "PCIe 4.0",
                        "ports", "3x DisplayPort 2.1, 1x HDMI 2.1",
                        "recommended_psu", "700W"
                )
        );

        createProductIfNotExists(
                cpus,
                "AMD Ryzen 7 7800X3D",
                new BigDecimal("449.00"),
                15,
                "amd-ryzen-7-7800x3d",
                List.of(
                        "https://images.unsplash.com/photo-1555680202-c86f0e12f086?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "socket", "AM5",
                        "cores", 8,
                        "threads", 16,
                        "base_clock", "4.2GHz",
                        "boost_clock", "5.0GHz",
                        "cache", "104MB 3D V-Cache"
                )
        );

        createProductIfNotExists(
                cpus,
                "Intel Core i9-14900K",
                new BigDecimal("589.00"),
                7,
                "intel-core-i9-14900k",
                List.of(
                        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1555680202-c86f0e12f086?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "socket", "LGA1700",
                        "cores", 24,
                        "threads", 32,
                        "boost_clock", "6.0GHz",
                        "cache", "36MB Intel Smart Cache"
                )
        );

        createProductIfNotExists(
                monitors,
                "Monitor Gamer ASUS TUF 27 IPS 180Hz",
                new BigDecimal("229.99"),
                20,
                "monitor-gamer-asus-tuf-27-ips-180hz",
                List.of(
                        "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1547119957-637f8679db1e?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "screen_size", "27 pulgadas",
                        "resolution", "2560x1440 QHD",
                        "refresh_rate", "180Hz",
                        "response_time", "1ms",
                        "panel", "Fast IPS"
                )
        );

        createProductIfNotExists(
                peripherals,
                "Teclado Mecanico Logitech G Pro X",
                new BigDecimal("129.99"),
                25,
                "teclado-mecanico-logitech-g-pro-x",
                List.of(
                        "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "switches", "GX Blue Clicky",
                        "layout", "TKL",
                        "connectivity", "USB extraible",
                        "lighting", "RGB Lightsync"
                )
        );

        createProductIfNotExists(
                peripherals,
                "Mouse Gamer Razer DeathAdder V3 Pro",
                new BigDecimal("149.99"),
                18,
                "mouse-gamer-razer-deathadder-v3-pro",
                List.of(
                        "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=800&q=80",
                        "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=800&q=80"
                ),
                Map.of(
                        "sensor", "Focus Pro 30K",
                        "weight", "63g",
                        "connectivity", "HyperSpeed Wireless",
                        "dpi", 30000
                )
        );

        log.info("[ProductSeeder] Catalog successfully seeded with categories, products, and images.");
    }

    private Category getOrCreateCategory(String slug, String name) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(new Category(slug, name, true)));
    }

    private void createProductIfNotExists(Category category, String name, BigDecimal price,
                                          Integer stock, String slug, List<String> imageUrls,
                                          Map<String, Object> specifications) {
        if (!productRepository.existsBySlug(slug)) {
            Product product = new Product(
                    category,
                    name,
                    price,
                    stock,
                    slug,
                    true,
                    null,
                    new HashMap<>(specifications)
            );
            if (imageUrls != null) {
                for (int i = 0; i < imageUrls.size(); i++) {
                    boolean isPrimary = (i == 0);
                    product.addImage(new ProductImage(imageUrls.get(i), isPrimary, i));
                }
            }
            productRepository.save(product);
        }
    }
}
