package com.turkcell.productservice.service.impl;

import com.turkcell.productservice.dto.request.ProductCreateRequest;
import com.turkcell.productservice.dto.request.ProductUpdateRequest;
import com.turkcell.productservice.dto.request.StockUpdateRequest;
import com.turkcell.productservice.dto.response.ProductPageResponse;
import com.turkcell.productservice.dto.response.ProductResponse;
import com.turkcell.productservice.dto.response.StockUpdateResponse;
import com.turkcell.productservice.entity.Product;
import com.turkcell.productservice.exception.ProductNotFoundException;
import com.turkcell.productservice.exception.SkuAlreadyExistsException;
import com.turkcell.productservice.mapper.ProductMapper;
import com.turkcell.productservice.repository.ProductRepository;
import com.turkcell.productservice.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResponse listProducts(int page, int size, String q) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.search(q, pageable);

        List<ProductResponse> items = productPage.getContent()
                .stream()
                .map(productMapper::toResponse)
                .toList();

        ProductPageResponse response = new ProductPageResponse();
        response.setItems(items);
        response.setPage(productPage.getNumber());
        response.setSize(productPage.getSize());
        response.setTotalItems(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        return response;
    }

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new SkuAlreadyExistsException(request.getSku());
        }
        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse replaceProduct(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new SkuAlreadyExistsException(request.getSku());
        }

        productMapper.updateEntity(product, request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public StockUpdateResponse updateStock(StockUpdateRequest request) {
        Product product = productRepository.findById(request.getId())
                .orElseThrow(() -> new ProductNotFoundException(request.getId()));
        product.setStock(request.getStock());
        Product saved = productRepository.save(product);
        return productMapper.toStockUpdateResponse(saved);
    }
}
