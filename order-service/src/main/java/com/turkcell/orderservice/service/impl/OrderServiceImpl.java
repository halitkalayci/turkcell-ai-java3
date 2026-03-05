package com.turkcell.orderservice.service.impl;

import com.turkcell.orderservice.client.ProductClient;
import com.turkcell.orderservice.client.dto.ProductResponse;
import com.turkcell.orderservice.dto.request.OrderCreateRequest;
import com.turkcell.orderservice.dto.request.OrderItemCreateRequest;
import com.turkcell.orderservice.dto.response.OrderPage;
import com.turkcell.orderservice.dto.response.OrderResponse;
import com.turkcell.orderservice.entity.Order;
import com.turkcell.orderservice.entity.OrderItem;
import com.turkcell.orderservice.entity.OrderStatus;
import com.turkcell.orderservice.exception.InsufficientStockException;
import com.turkcell.orderservice.exception.OrderNotFoundException;
import com.turkcell.orderservice.mapper.OrderMapper;
import com.turkcell.orderservice.repository.OrderRepository;
import com.turkcell.orderservice.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductClient productClient,
                            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemCreateRequest itemRequest : request.getItems()) {
            ProductResponse product = productClient.getProductById(itemRequest.getProductId());

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        itemRequest.getProductId(),
                        product.getStock(),
                        itemRequest.getQuantity()
                );
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(itemRequest.getQuantity());

            orderItems.add(item);
            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderPage listOrders(int page, int size) {
        return orderMapper.toPage(orderRepository.findAll(PageRequest.of(page, size)));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new com.turkcell.orderservice.exception.InvalidStatusTransitionException(
                    order.getStatus(), OrderStatus.CANCELLED
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }
}
