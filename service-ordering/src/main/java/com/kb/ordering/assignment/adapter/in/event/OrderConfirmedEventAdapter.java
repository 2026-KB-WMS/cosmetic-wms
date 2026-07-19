package com.kb.ordering.assignment.adapter.in.event;

import com.kb.ordering.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// @Component
@RequiredArgsConstructor
public class OrderConfirmedEventAdapter {

    private final RetryableWarehouseAssigner retryableWarehouseAssigner;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        retryableWarehouseAssigner.assignWithRetry(event);
    }
}
