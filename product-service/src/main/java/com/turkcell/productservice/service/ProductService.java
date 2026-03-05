package com.turkcell.productservice.service;

import com.turkcell.productservice.dto.request.ProductCreateRequest;
import com.turkcell.productservice.dto.request.ProductUpdateRequest;
import com.turkcell.productservice.dto.request.StockUpdateRequest;
import com.turkcell.productservice.dto.response.ProductPageResponse;
import com.turkcell.productservice.dto.response.ProductResponse;
import com.turkcell.productservice.dto.response.StockUpdateResponse;

import java.util.UUID;

public interface ProductService {

    ProductPageResponse listProducts(int page, int size, String q);

    ProductResponse createProduct(ProductCreateRequest request);

    ProductResponse getProductById(UUID id);

    ProductResponse replaceProduct(UUID id, ProductUpdateRequest request);

    void deleteProduct(UUID id);

    StockUpdateResponse updateStock(StockUpdateRequest request);
}
