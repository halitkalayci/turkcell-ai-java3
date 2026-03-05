package com.turkcell.orderservice.adapter.out.persistence;

import com.turkcell.orderservice.domain.model.Order;
import com.turkcell.orderservice.domain.port.out.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    public OrderPersistenceAdapter(OrderJpaRepository jpaRepository,
                                   OrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Order> findAll(int page, int size) {
        return jpaRepository.findAll(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
