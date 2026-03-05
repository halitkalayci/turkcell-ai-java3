package com.turkcell.orderservice.service;

import com.turkcell.orderservice.dto.request.OrderCreateRequest;
import com.turkcell.orderservice.dto.response.OrderPage;
import com.turkcell.orderservice.dto.response.OrderResponse;

import java.util.UUID;

public interface OrderService {

    OrderResponse createOrder(OrderCreateRequest request);

    OrderPage listOrders(int page, int size);

    OrderResponse getOrderById(UUID id);

    OrderResponse cancelOrder(UUID id);
}
