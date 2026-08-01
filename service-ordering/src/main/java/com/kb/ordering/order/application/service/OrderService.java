package com.kb.ordering.order.application.service;

import com.kb.ordering.order.application.port.in.CreateOrderCommand;
import com.kb.ordering.order.application.port.in.OrderLifecycleUseCase;
import com.kb.ordering.order.application.port.in.OrderResult;
import com.kb.ordering.order.application.port.out.EventPublisher;
import com.kb.ordering.order.application.port.out.OrderPort;
import com.kb.ordering.order.domain.event.OrderConfirmedEvent;
import com.kb.ordering.order.domain.exception.OrderNotFoundException;
import com.kb.ordering.order.domain.model.Order;
import com.kb.ordering.order.domain.model.OrderLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        eventPublisher.publishOrderConfirmed(toOrderConfirmedEvent(saved));
        return OrderResult.from(saved);
    }

    @Override
    @Transactional
    public OrderResult assignWarehouse(Long orderId, Long warehouseId) {
        Order order = findByIdForUpdateOrThrow(orderId);
        order.assignWarehouse(warehouseId);
        return OrderResult.from(orderPort.save(order));
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
        List<OrderConfirmedEvent.ItemSnapshot> snapshots = order.getOrderItems().stream()
                .map(item -> new OrderConfirmedEvent.ItemSnapshot(item.getId(), item.getProductId(), item.getQuantity()))
                .toList();
        return new OrderConfirmedEvent(order.getId(), order.getStoreId(), snapshots);
    }
}
