package com.kb.cosmetic_wms.order.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import com.kb.cosmetic_wms.order.application.port.in.CreateOrderCommand;
import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import com.kb.cosmetic_wms.order.application.port.in.OrderResult;
import com.kb.cosmetic_wms.order.application.port.out.OrderPort;
import com.kb.cosmetic_wms.order.domain.exception.OrderNotFoundException;
import com.kb.cosmetic_wms.order.domain.model.Order;
import com.kb.cosmetic_wms.order.domain.model.OrderLine;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService implements OrderLifecycleUseCase {

    private final OrderPort orderPort;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResult createOrder(CreateOrderCommand command) {
        List<OrderLine> lines = command.lines().stream()
                .map(l -> new OrderLine(l.productId(), l.quantity()))
                .toList();
        Order order = Order.create(command.storeId(), lines);
        return OrderResult.from(orderPort.save(order));
    }

    @Override
    @Transactional
    public OrderResult confirmOrder(Long orderId) {
        Order order = findByIdWithItemsForUpdateOrThrow(orderId);
        order.confirm();
        Order saved = orderPort.save(order);
        eventPublisher.publish(toOrderConfirmedEvent(saved));
        return OrderResult.from(saved);
    }

    @Override
    @Transactional
    public OrderResult startPreparation(Long orderId) {
        Order order = findByIdForUpdateOrThrow(orderId);
        order.startPreparation();
        return OrderResult.from(orderPort.save(order));
    }

    @Override
    @Transactional
    public OrderResult ship(Long orderId) {
        Order order = findByIdForUpdateOrThrow(orderId);
        order.ship();
        return OrderResult.from(orderPort.save(order));
    }

    @Override
    @Transactional
    public OrderResult completeDelivery(Long orderId) {
        Order order = findByIdForUpdateOrThrow(orderId);
        order.completeDelivery();
        return OrderResult.from(orderPort.save(order));
    }

    @Override
    @Transactional
    public OrderResult cancelOrder(Long orderId) {
        Order order = findByIdForUpdateOrThrow(orderId);
        order.cancel();
        return OrderResult.from(orderPort.save(order));
    }

    private Order findByIdForUpdateOrThrow(Long orderId) {
        return orderPort.findByIdForUpdate(orderId).orElseThrow(OrderNotFoundException::new);
    }

    private Order findByIdWithItemsForUpdateOrThrow(Long orderId) {
        return orderPort.findByIdWithItemsForUpdate(orderId).orElseThrow(OrderNotFoundException::new);
    }

    private OrderConfirmedEvent toOrderConfirmedEvent(Order order) {
        var snapshots = order.getOrderItems().stream()
                .map(item -> new OrderConfirmedEvent.ItemSnapshot(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderConfirmedEvent(order.getId(), order.getWarehouseId(), snapshots);
    }
}