package com.turkcell.orderservice.domain.port.in;

import com.turkcell.orderservice.domain.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Order createOrder(Order order);

    List<Order> listOrders(int page, int size);

    long countOrders();

    Order findById(UUID id);

    Order cancelOrder(UUID id);
}
