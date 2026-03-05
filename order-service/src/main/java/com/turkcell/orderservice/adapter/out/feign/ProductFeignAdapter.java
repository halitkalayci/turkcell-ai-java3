package com.turkcell.orderservice.adapter.out.feign;

import com.turkcell.orderservice.adapter.out.feign.dto.ProductFeignResponse;
import com.turkcell.orderservice.domain.model.Product;
import com.turkcell.orderservice.domain.model.ProductNotFoundException;
import com.turkcell.orderservice.domain.port.out.ProductClient;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProductFeignAdapter implements ProductClient {

    private final ProductFeignClient feignClient;

    public ProductFeignAdapter(ProductFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    @Override
    public Product getProductById(UUID id) {
        try {
            ProductFeignResponse response = feignClient.getProductById(id);
            Product product = new Product();
            product.setId(response.getId());
            product.setName(response.getName());
            product.setPrice(response.getPrice());
            product.setStock(response.getStock());
            return product;
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(id);
        }
    }
}
