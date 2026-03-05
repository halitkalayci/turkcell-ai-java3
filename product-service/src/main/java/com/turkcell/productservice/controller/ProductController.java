package com.turkcell.productservice.controller;

import com.turkcell.productservice.dto.request.ProductCreateRequest;
import com.turkcell.productservice.dto.request.ProductUpdateRequest;
import com.turkcell.productservice.dto.request.StockUpdateRequest;
import com.turkcell.productservice.dto.response.ProductPageResponse;
import com.turkcell.productservice.dto.response.ProductResponse;
import com.turkcell.productservice.dto.response.StockUpdateResponse;
import com.turkcell.productservice.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ProductPageResponse> listProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(productService.listProducts(page, size, q));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Product.Create')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('Product.Update')")
    public ResponseEntity<ProductResponse> replaceProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.replaceProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('Product.Delete')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-stock")
    @PreAuthorize("hasAuthority('Product.UpdateStock')")
    public ResponseEntity<StockUpdateResponse> updateStock(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(productService.updateStock(request));
    }
}
