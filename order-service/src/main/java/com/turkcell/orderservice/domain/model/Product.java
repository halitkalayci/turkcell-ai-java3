package com.turkcell.orderservice.domain.model;

import java.util.UUID;

public class Product {

    private UUID id;
    private String name;
    private Double price;
    private Integer stock;

    public Product() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
