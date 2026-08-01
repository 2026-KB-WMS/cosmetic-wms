package com.kb.ordering.order.application.port.in;

public interface OrderLifecycleUseCase {

    OrderResult createOrder(CreateOrderCommand command);

    OrderResult confirmOrder(Long orderId);

    OrderResult assignWarehouse(Long orderId, Long warehouseId);

    OrderResult startPreparation(Long orderId);

    OrderResult ship(Long orderId);

    OrderResult completeDelivery(Long orderId);

    OrderResult cancelOrder(Long orderId);
}
