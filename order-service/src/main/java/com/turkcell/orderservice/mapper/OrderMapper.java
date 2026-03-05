package com.turkcell.orderservice.mapper;

import com.turkcell.orderservice.dto.response.OrderItemResponse;
import com.turkcell.orderservice.dto.response.OrderPage;
import com.turkcell.orderservice.dto.response.OrderResponse;
import com.turkcell.orderservice.entity.Order;
import com.turkcell.orderservice.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setTotalAmount(order.getTotalAmount());
        response.setOrderDate(order.getOrderDate());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setItems(order.getItems().stream().map(this::toItemResponse).toList());
        return response;
    }

    public OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setUnitPrice(item.getUnitPrice());
        response.setQuantity(item.getQuantity());
        return response;
    }

    public OrderPage toPage(Page<Order> page) {
        OrderPage orderPage = new OrderPage();
        orderPage.setItems(page.getContent().stream().map(this::toResponse).toList());
        orderPage.setPage(page.getNumber());
        orderPage.setSize(page.getSize());
        orderPage.setTotalItems(page.getTotalElements());
        orderPage.setTotalPages(page.getTotalPages());
        return orderPage;
    }
}
