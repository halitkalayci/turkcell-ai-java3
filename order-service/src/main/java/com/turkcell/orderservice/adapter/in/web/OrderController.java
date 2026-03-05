package com.turkcell.orderservice.adapter.in.web;

import com.turkcell.orderservice.adapter.in.web.dto.request.OrderCreateRequest;
import com.turkcell.orderservice.adapter.in.web.dto.response.OrderPage;
import com.turkcell.orderservice.adapter.in.web.dto.response.OrderResponse;
import com.turkcell.orderservice.adapter.in.web.mapper.OrderWebMapper;
import com.turkcell.orderservice.domain.model.Order;
import com.turkcell.orderservice.domain.port.in.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final OrderWebMapper orderWebMapper;

    public OrderController(OrderService orderService, OrderWebMapper orderWebMapper) {
        this.orderService = orderService;
        this.orderWebMapper = orderWebMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('Order.Create')")
    @Operation(summary = "Yeni sipariş oluştur")
    @ApiResponse(responseCode = "201", description = "Sipariş oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Validasyon hatası")
    @ApiResponse(responseCode = "409", description = "Stok yetersiz")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderWebMapper.toDomain(request);
        Order created = orderService.createOrder(order);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(orderWebMapper.toResponse(created));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Order.Read')")
    @Operation(summary = "Siparişleri listele")
    @ApiResponse(responseCode = "200", description = "Başarılı")
    public ResponseEntity<OrderPage> listOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        List<Order> orders = orderService.listOrders(page, size);
        long totalItems = orderService.countOrders();

        return ResponseEntity.ok(orderWebMapper.toOrderPage(orders, page, size, totalItems));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('Order.Read')")
    @Operation(summary = "Sipariş detayı getir")
    @ApiResponse(responseCode = "200", description = "Başarılı")
    @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(orderWebMapper.toResponse(order));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('Order.Cancel')")
    @Operation(summary = "Sipariş iptal et")
    @ApiResponse(responseCode = "200", description = "Sipariş iptal edildi")
    @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı")
    @ApiResponse(responseCode = "409", description = "Durum geçişi geçersiz veya sipariş zaten iptal edilmiş")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        Order cancelled = orderService.cancelOrder(id);
        return ResponseEntity.ok(orderWebMapper.toResponse(cancelled));
    }
}
