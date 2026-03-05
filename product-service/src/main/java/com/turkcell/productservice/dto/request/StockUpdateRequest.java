package com.turkcell.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class StockUpdateRequest {

    @NotNull
    private UUID id;

    @NotNull
    @Min(0)
    private Integer stock;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
}
