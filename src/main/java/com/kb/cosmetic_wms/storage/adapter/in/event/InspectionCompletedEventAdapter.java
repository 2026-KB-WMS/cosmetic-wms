package com.kb.cosmetic_wms.storage.adapter.in.event;

import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.storage.application.port.in.ReduceDockingCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.ReduceDockingCapacityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InspectionCompletedEventAdapter {

    private final ReduceDockingCapacityUseCase reduceDockingCapacityUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InspectionCompletedEvent event) {
        int totalQuantity = event.passedQuantity() + event.failedQuantity();
        if (totalQuantity <= 0) {
            return;
        }
        reduceDockingCapacityUseCase.reduceDockingCapacity(
                new ReduceDockingCapacityCommand(
                        event.warehouseId(),
                        List.of(new ReduceDockingCapacityCommand.LineItem(event.productId(), totalQuantity))
                )
        );
    }
}
