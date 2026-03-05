package com.turkcell.orderservice.domain.model;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    private final UUID productId;
    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(UUID productId, int availableStock, int requestedQuantity) {
        super(String.format(
                "Insufficient stock for product %s. Available: %d, Requested: %d",
                productId, availableStock, requestedQuantity
        ));
        this.productId = productId;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public UUID getProductId() { return productId; }
    public int getAvailableStock() { return availableStock; }
    public int getRequestedQuantity() { return requestedQuantity; }
}
