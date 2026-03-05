package com.turkcell.orderservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class OrderCreateRequest {

    @NotNull
    private UUID customerId;

    @NotBlank
    @Size(max = 500)
    private String shippingAddress;

    @NotEmpty
    @Valid
    private List<OrderItemCreateRequest> items;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public List<OrderItemCreateRequest> getItems() { return items; }
    public void setItems(List<OrderItemCreateRequest> items) { this.items = items; }
}
