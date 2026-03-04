package com.turkcell.productservice.mapper;

import com.turkcell.productservice.dto.request.ProductCreateRequest;
import com.turkcell.productservice.dto.request.ProductUpdateRequest;
import com.turkcell.productservice.dto.response.ProductResponse;
import com.turkcell.productservice.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setSku(product.getSku());
        return response;
    }

    public Product toEntity(ProductCreateRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
        return product;
    }

    public void updateEntity(Product product, ProductUpdateRequest request) {
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setSku(request.getSku());
    }
}
