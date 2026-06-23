package com.kb.cosmetic_wms.domain.order.event;

import com.kb.cosmetic_wms.domain.order.service.OrderService;
import com.kb.cosmetic_wms.global.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.global.event.OutboundShippedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundAllocated(OutboundAllocatedEvent event) {
        orderService.startPreparation(event.ordersId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundShipped(OutboundShippedEvent event) {
        orderService.ship(event.ordersId());
    }
}