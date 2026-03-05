package com.turkcell.orderservice.domain.model;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order not found with id: " + id);
    }
}
