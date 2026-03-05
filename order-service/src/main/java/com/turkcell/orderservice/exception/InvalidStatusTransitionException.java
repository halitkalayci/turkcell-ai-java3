package com.turkcell.orderservice.exception;

import com.turkcell.orderservice.entity.OrderStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(OrderStatus current, OrderStatus target) {
        super(String.format("Cannot transition order from %s to %s", current, target));
    }
}
