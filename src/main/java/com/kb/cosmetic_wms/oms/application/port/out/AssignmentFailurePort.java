package com.kb.cosmetic_wms.oms.application.port.out;

import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;

public interface AssignmentFailurePort {

    void save(OrderConfirmedEvent event, String errorMessage);
}
