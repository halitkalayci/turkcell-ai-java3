package com.turkcell.orderservice.adapter.in.web.mapper;

import com.turkcell.orderservice.adapter.in.web.dto.request.OrderCreateRequest;
import com.turkcell.orderservice.adapter.in.web.dto.response.OrderItemResponse;
import com.turkcell.orderservice.adapter.in.web.dto.response.OrderPage;
import com.turkcell.orderservice.adapter.in.web.dto.response.OrderResponse;
import com.turkcell.orderservice.domain.model.Order;
import com.turkcell.orderservice.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderWebMapper {

    public Order toDomain(OrderCreateRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setShippingAddress(request.getShippingAddress());

        List<OrderItem> items = request.getItems().stream().map(itemReq -> {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            return item;
        }).toList();

        order.setItems(items);
        return order;
    }

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

    public OrderPage toOrderPage(List<Order> orders, int page, int size, long totalItems) {
        OrderPage orderPage = new OrderPage();
        orderPage.setItems(orders.stream().map(this::toResponse).toList());
        orderPage.setPage(page);
        orderPage.setSize(size);
        orderPage.setTotalItems(totalItems);
        orderPage.setTotalPages((int) Math.ceil((double) totalItems / size));
        return orderPage;
    }
}
