package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component("inspectionInboundCompletedEventAdapter")
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final RetryableInspectionCreator retryableInspectionCreator;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInboundCompleted(InboundCompletedEvent event) {
        for (var line : event.lines()) {
            if (line.receivedQuantity() <= 0) {
                continue;
            }
            retryableInspectionCreator.createWithRetry(event, line);
        }
    }
}