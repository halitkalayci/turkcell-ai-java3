package com.turkcell.productservice.dto.request;

import jakarta.validation.constraints.*;

public class ProductCreateRequest {

    @NotBlank
    @Size(min = 1, max = 200)
    private String name;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double price;

    @NotNull
    @Min(1)
    private Integer stock;

    @NotBlank
    @Size(min = 1, max = 64)
    private String sku;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
}
