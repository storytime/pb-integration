package com.github.storytime.model.db;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@MappedSuperclass
public class BaseEntity {

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;

    public Long getId() {
        return id;
    }

    public BaseEntity setId(Long id) {
        this.id = id;
        return this;
    }
}
