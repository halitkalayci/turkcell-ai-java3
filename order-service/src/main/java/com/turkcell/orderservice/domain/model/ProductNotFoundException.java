package com.turkcell.orderservice.domain.model;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(UUID id) {
        super("Product not found with id: " + id);
    }
}
