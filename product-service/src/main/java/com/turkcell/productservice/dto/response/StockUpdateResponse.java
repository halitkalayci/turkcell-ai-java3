package com.turkcell.productservice.dto.response;

import java.util.UUID;

public class StockUpdateResponse {

    private UUID id;
    private String name;
    private Integer stock;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
