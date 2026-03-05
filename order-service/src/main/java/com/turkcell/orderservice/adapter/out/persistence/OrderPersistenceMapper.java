package com.turkcell.orderservice.adapter.out.persistence;

import com.turkcell.orderservice.domain.model.Order;
import com.turkcell.orderservice.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderJpaEntity entity) {
        Order order = new Order();
        order.setId(entity.getId());
        order.setCustomerId(entity.getCustomerId());
        order.setStatus(entity.getStatus());
        order.setShippingAddress(entity.getShippingAddress());
        order.setTotalAmount(entity.getTotalAmount());
        order.setOrderDate(entity.getOrderDate());
        order.setCreatedAt(entity.getCreatedAt());
        order.setUpdatedAt(entity.getUpdatedAt());
        order.setItems(entity.getItems().stream().map(this::toItemDomain).toList());
        return order;
    }

    public OrderItem toItemDomain(OrderItemJpaEntity entity) {
        OrderItem item = new OrderItem();
        item.setId(entity.getId());
        item.setProductId(entity.getProductId());
        item.setProductName(entity.getProductName());
        item.setUnitPrice(entity.getUnitPrice());
        item.setQuantity(entity.getQuantity());
        return item;
    }

    public OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setShippingAddress(order.getShippingAddress());
        entity.setTotalAmount(order.getTotalAmount());
        entity.setOrderDate(order.getOrderDate());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemJpaEntity> jpaItems = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemJpaEntity jpaItem = toItemJpaEntity(item);
            jpaItem.setOrder(entity);
            jpaItems.add(jpaItem);
        }
        entity.setItems(jpaItems);

        return entity;
    }

    public OrderItemJpaEntity toItemJpaEntity(OrderItem item) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setId(item.getId());
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setQuantity(item.getQuantity());
        return entity;
    }
}
