package com.turkcell.orderservice.application.service;

import com.turkcell.orderservice.domain.model.InsufficientStockException;
import com.turkcell.orderservice.domain.model.Order;
import com.turkcell.orderservice.domain.model.OrderAlreadyCancelledException;
import com.turkcell.orderservice.domain.model.OrderItem;
import com.turkcell.orderservice.domain.model.OrderNotFoundException;
import com.turkcell.orderservice.domain.model.OrderStatus;
import com.turkcell.orderservice.domain.model.InvalidStatusTransitionException;
import com.turkcell.orderservice.domain.model.Product;
import com.turkcell.orderservice.domain.port.in.OrderService;
import com.turkcell.orderservice.domain.port.out.OrderRepository;
import com.turkcell.orderservice.domain.port.out.ProductClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    public OrderServiceImpl(OrderRepository orderRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @Override
    public Order createOrder(Order order) {
        List<OrderItem> enrichedItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItem item : order.getItems()) {
            Product product = productClient.getProductById(item.getProductId());

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        item.getProductId(),
                        product.getStock(),
                        item.getQuantity()
                );
            }

            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            enrichedItems.add(item);
            totalAmount += product.getPrice() * item.getQuantity();
        }

        order.setItems(enrichedItems);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> listOrders(int page, int size) {
        return orderRepository.findAll(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public long countOrders() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public Order cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderAlreadyCancelledException(id);
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidStatusTransitionException(order.getStatus(), OrderStatus.CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
