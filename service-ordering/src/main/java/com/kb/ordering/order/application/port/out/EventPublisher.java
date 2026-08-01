package com.kb.ordering.order.application.port.out;

import com.kb.ordering.order.domain.event.OrderConfirmedEvent;

public interface


EventPublisher {
    void publishOrderConfirmed(OrderConfirmedEvent event);
}
