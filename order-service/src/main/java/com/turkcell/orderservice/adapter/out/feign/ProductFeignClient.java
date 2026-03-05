package com.turkcell.orderservice.adapter.out.feign;

import com.turkcell.orderservice.adapter.out.feign.dto.ProductFeignResponse;
import com.turkcell.orderservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", configuration = FeignClientConfig.class)
public interface ProductFeignClient {

    @GetMapping("/api/v1/products/{id}")
    ProductFeignResponse getProductById(@PathVariable("id") UUID id);
}
