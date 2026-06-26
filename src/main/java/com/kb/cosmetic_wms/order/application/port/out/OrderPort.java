package com.kb.cosmetic_wms.order.application.port.out;

import com.kb.cosmetic_wms.order.domain.model.Order;

import java.util.Optional;

public interface OrderPort {

    Optional<Order> findByIdForUpdate(Long id);

    Optional<Order> findByIdWithItemsForUpdate(Long id);

    Order save(Order order);
}