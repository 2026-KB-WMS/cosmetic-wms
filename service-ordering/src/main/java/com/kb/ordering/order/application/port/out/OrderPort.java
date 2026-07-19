package com.kb.ordering.order.application.port.out;

import com.kb.ordering.order.domain.model.Order;

import java.util.Optional;

public interface OrderPort {

    Optional<Order> findByIdForUpdate(Long id);

    Optional<Order> findByIdWithItemsForUpdate(Long id);

    Order save(Order order);
}