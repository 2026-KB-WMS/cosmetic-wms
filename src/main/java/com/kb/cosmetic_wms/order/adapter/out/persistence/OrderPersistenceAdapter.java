package com.kb.cosmetic_wms.order.adapter.out.persistence;

import com.kb.cosmetic_wms.order.application.port.out.OrderPort;
import com.kb.cosmetic_wms.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPort {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findByIdForUpdate(Long id) {
        return orderJpaRepository.findByIdForUpdate(id).map(OrderEntity::toDomain);
    }

    @Override
    public Optional<Order> findByIdWithItemsForUpdate(Long id) {
        return orderJpaRepository.findByIdWithItemsForUpdate(id).map(OrderEntity::toDomain);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
    }
}