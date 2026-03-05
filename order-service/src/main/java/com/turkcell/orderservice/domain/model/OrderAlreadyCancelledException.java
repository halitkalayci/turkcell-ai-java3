package com.turkcell.orderservice.domain.model;

import java.util.UUID;

public class OrderAlreadyCancelledException extends RuntimeException {

    public OrderAlreadyCancelledException(UUID orderId) {
        super("Order is already cancelled: " + orderId);
    }
}
