package com.turkcell.orderservice.domain.port.out;

import com.turkcell.orderservice.domain.model.Product;

import java.util.UUID;

public interface ProductClient {

    Product getProductById(UUID id);
}
