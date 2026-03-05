package com.turkcell.orderservice.adapter.in.web.dto.response;

import java.util.UUID;

public class OrderItemResponse {

    private UUID productId;
    private String productName;
    private Double unitPrice;
    private Integer quantity;

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
