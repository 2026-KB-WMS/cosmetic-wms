package com.kb.cosmetic_wms.domain.order.service;

import com.kb.cosmetic_wms.domain.order.OrderLine;
import com.kb.cosmetic_wms.domain.order.dto.CreateOrderRequestDto;
import com.kb.cosmetic_wms.domain.order.dto.OrderResponseDto;
import com.kb.cosmetic_wms.domain.order.entity.Orders;
import com.kb.cosmetic_wms.global.event.OrderConfirmedEvent;
import com.kb.cosmetic_wms.domain.order.exception.OrderNotFoundException;
import com.kb.cosmetic_wms.domain.order.repository.OrderRepository;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto request) {
        List<OrderLine> lines = request.items().stream()
                .map(item -> new OrderLine(item.productId(), item.quantity()))
                .toList();
        Orders order = Orders.create(request.storeId(), request.warehouseId(), lines);
        return OrderResponseDto.from(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto confirmOrder(Long orderId) {
        Orders order = findByIdWithOrderItemsForUpdateOrThrow(orderId);
        order.confirm();
        eventPublisher.publish(toOrderConfirmedEvent(order));
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto startPreparation(Long orderId) {
        Orders order = findByIdForUpdateOrThrow(orderId);
        order.startPreparation();
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto ship(Long orderId) {
        Orders order = findByIdForUpdateOrThrow(orderId);
        order.ship();
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto completeDelivery(Long orderId) {
        Orders order = findByIdForUpdateOrThrow(orderId);
        order.completeDelivery();
        return OrderResponseDto.from(order);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        Orders order = findByIdForUpdateOrThrow(orderId);
        order.cancel();
        return OrderResponseDto.from(order);
    }

    private OrderConfirmedEvent toOrderConfirmedEvent(Orders order) {
        var snapshots = order.getOrderItems().stream()
                .map(item -> new OrderConfirmedEvent.ItemSnapshot(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderConfirmedEvent(order.getId(), order.getWarehouseId(), snapshots);
    }

    private Orders findByIdForUpdateOrThrow(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    private Orders findByIdWithOrderItemsForUpdateOrThrow(Long orderId) {
        return orderRepository.findByIdWithOrderItemsForUpdate(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }
}