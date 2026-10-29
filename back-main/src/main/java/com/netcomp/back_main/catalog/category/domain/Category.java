package com.netcomp.back_main.catalog.category.domain;

import com.netcomp.back_main.catalog.product.domain.Product;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean active = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specification_schema", columnDefinition = "jsonb")
    private List<CategorySpecificationDefinition> specificationSchema = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // relationships
    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    // constructors
    public Category() {
    }

    public Category(String slug, String name, Boolean active) {
        this.slug = slug;
        this.name = name;
        this.active = active;
    }

    public Category(String slug, String name, Boolean active, List<Product> products) {
        this.slug = slug;
        this.name = name;
        this.active = active;
        this.products = products != null ? products : new ArrayList<>();
    }

    public Category(UUID id, Instant createdAt, String slug, String name, Boolean active, List<Product> products) {
        this.id = id;
        this.createdAt = createdAt;
        this.slug = slug;
        this.name = name;
        this.active = active;
        this.products = products != null ? products : new ArrayList<>();
    }

    // getters
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return active;
    }

    public List<CategorySpecificationDefinition> getSpecificationSchema() {
        return specificationSchema;
    }

    public List<Product> getProducts() {
        return products;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setSpecificationSchema(List<CategorySpecificationDefinition> specificationSchema) {
        this.specificationSchema = specificationSchema != null
                ? new ArrayList<>(specificationSchema)
                : new ArrayList<>();
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
