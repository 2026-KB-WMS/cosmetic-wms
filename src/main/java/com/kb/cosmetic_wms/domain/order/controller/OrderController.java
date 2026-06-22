package com.kb.cosmetic_wms.domain.order.controller;

import com.kb.cosmetic_wms.domain.order.dto.CreateOrderRequestDto;
import com.kb.cosmetic_wms.domain.order.dto.OrderResponseDto;
import com.kb.cosmetic_wms.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponseDto> confirmOrder(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.confirmOrder(orderId));
    }

    @PatchMapping("/{orderId}/prepare")
    public ResponseEntity<OrderResponseDto> startPreparation(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.startPreparation(orderId));
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponseDto> ship(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.ship(orderId));
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponseDto> completeDelivery(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.completeDelivery(orderId));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(
            @PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}