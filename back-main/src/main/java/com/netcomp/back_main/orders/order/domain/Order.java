package com.netcomp.back_main.orders.order.domain;

import com.netcomp.back_main.orders.payment.domain.Payment;
import com.netcomp.back_main.users.user.domain.User;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "shipping_address", nullable = false, length = 255)
    private String shippingAddress;

    // relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_code", nullable = false)
    private OrderStatus status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @BatchSize(size = 50)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // constructors
    public Order() {
    }

    public Order(User user, OrderStatus status, BigDecimal total, String shippingAddress) {
        this.user = user;
        this.status = status;
        this.total = total;
        this.shippingAddress = shippingAddress;
    }

    public Order(UUID id, Instant createdAt, User user, OrderStatus status, BigDecimal total,
                 String shippingAddress, Payment payment, List<OrderItem> items) {
        this.id = id;
        this.createdAt = createdAt;
        this.user = user;
        this.status = status;
        this.total = total;
        this.shippingAddress = shippingAddress;
        this.payment = payment;
        this.items = items != null ? items : new ArrayList<>();
    }

    // getters
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public Payment getPayment() {
        return payment;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    // setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
