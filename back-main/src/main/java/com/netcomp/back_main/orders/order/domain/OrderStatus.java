package com.netcomp.back_main.orders.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_statuses")
public class OrderStatus {

    @Id
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // constructors
    public OrderStatus() {
    }

    public OrderStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // getters
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    // setters
    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }
}
