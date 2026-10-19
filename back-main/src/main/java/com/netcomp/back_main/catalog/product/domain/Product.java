package com.netcomp.back_main.catalog.product.domain;

import com.netcomp.back_main.catalog.category.domain.Category;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false, unique = true, length = 150)
    private String slug;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(columnDefinition = "vector(768)")
    private String embedding;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specifications", columnDefinition = "jsonb")
    private Map<String, Object> specifications = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // constructors
    public Product() {
    }

    public Product(Category category, String name, BigDecimal price, Integer stock, String slug,
                   Boolean active, String embedding, Map<String, Object> specifications) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.slug = slug;
        this.active = active;
        this.embedding = embedding;
        this.specifications = specifications != null ? specifications : new HashMap<>();
    }

    public Product(UUID id, Instant createdAt, Category category, String name, BigDecimal price,
                   Integer stock, String slug, Boolean active, String embedding,
                   Map<String, Object> specifications) {
        this.id = id;
        this.createdAt = createdAt;
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.slug = slug;
        this.active = active;
        this.embedding = embedding;
        this.specifications = specifications != null ? specifications : new HashMap<>();
    }

    // helper methods for images
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

    // getters
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getSlug() {
        return slug;
    }

    public Boolean getActive() {
        return active;
    }

    public String getEmbedding() {
        return embedding;
    }

    public List<ProductImage> getImages() {
        return images;
    }

    public Map<String, Object> getSpecifications() {
        return specifications;
    }

    public Category getCategory() {
        return category;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }

    public void setSpecifications(Map<String, Object> specifications) {
        this.specifications = specifications;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
