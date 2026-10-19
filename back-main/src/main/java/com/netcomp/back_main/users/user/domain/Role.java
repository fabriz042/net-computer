package com.netcomp.back_main.users.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    // constructors
    public Role() {
    }

    public Role(String code, String name) {
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
