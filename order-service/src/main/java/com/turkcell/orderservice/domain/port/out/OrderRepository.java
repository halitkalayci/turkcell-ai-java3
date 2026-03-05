package com.turkcell.orderservice.domain.port.out;

import com.turkcell.orderservice.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    List<Order> findAll(int page, int size);

    long count();
}
