package com.kb.ordering.order.adapter.in.web;

import com.kb.ordering.order.application.port.in.OrderLifecycleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderLifecycleUseCase orderLifecycleUseCase;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderResponse.from(orderLifecycleUseCase.createOrder(request.toCommand())));
    }

    @PatchMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderLifecycleUseCase.confirmOrder(orderId)));
    }

    @PatchMapping("/{orderId}/prepare")
    public ResponseEntity<OrderResponse> startPreparation(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderLifecycleUseCase.startPreparation(orderId)));
    }

    @PatchMapping("/{orderId}/ship")
    public ResponseEntity<OrderResponse> ship(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderLifecycleUseCase.ship(orderId)));
    }

    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> completeDelivery(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderLifecycleUseCase.completeDelivery(orderId)));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderLifecycleUseCase.cancelOrder(orderId)));
    }
}