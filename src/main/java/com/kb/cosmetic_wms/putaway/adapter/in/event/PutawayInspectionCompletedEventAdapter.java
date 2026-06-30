package com.kb.cosmetic_wms.putaway.adapter.in.event;

import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.putaway.application.port.in.CreatePutawayOrderCommand;
import com.kb.cosmetic_wms.putaway.application.port.in.CreatePutawayOrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PutawayInspectionCompletedEventAdapter {

    private final CreatePutawayOrderUseCase createPutawayOrderUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(InspectionCompletedEvent event) {
        if (event.passedQuantity() <= 0 && event.failedQuantity() <= 0) {
            return;
        }
        createPutawayOrderUseCase.create(new CreatePutawayOrderCommand(
                event.inspectionId(),
                event.lotId(),
                event.productId(),
                event.warehouseId(),
                event.passedQuantity(),
                event.failedQuantity()
        ));
    }
}
