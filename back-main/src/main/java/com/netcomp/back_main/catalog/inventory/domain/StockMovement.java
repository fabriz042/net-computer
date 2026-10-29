package com.netcomp.back_main.catalog.inventory.domain;

import com.netcomp.back_main.catalog.product.domain.Product;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // constructors
    public StockMovement() {
    }

    public StockMovement(Product product, MovementType type, Integer quantity,
                         Integer stockBefore, Integer stockAfter, String reason, String referenceId) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.reason = reason;
        this.referenceId = referenceId;
    }

    public StockMovement(UUID id, Instant createdAt, Product product, MovementType type, Integer quantity,
                         Integer stockBefore, Integer stockAfter, String reason, String referenceId) {
        this.id = id;
        this.createdAt = createdAt;
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.reason = reason;
        this.referenceId = referenceId;
    }

    // getters
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public MovementType getType() {
        return type;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getStockBefore() {
        return stockBefore;
    }

    public Integer getStockAfter() {
        return stockAfter;
    }

    public String getReason() {
        return reason;
    }

    public String getReferenceId() {
        return referenceId;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setStockBefore(Integer stockBefore) {
        this.stockBefore = stockBefore;
    }

    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}
