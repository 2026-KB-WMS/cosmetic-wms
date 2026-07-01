package com.kb.cosmetic_wms.order.adapter.in.event;

import com.kb.cosmetic_wms.outbound.domain.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderLifecycleUseCase orderLifecycleUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundAllocated(OutboundAllocatedEvent event) {
        orderLifecycleUseCase.startPreparation(event.ordersId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundShipped(OutboundShippedEvent event) {
        orderLifecycleUseCase.ship(event.ordersId());
    }
}