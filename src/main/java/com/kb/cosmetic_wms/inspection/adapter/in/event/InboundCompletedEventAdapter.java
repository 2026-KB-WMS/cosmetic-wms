package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.InspectionLifecycleUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final InspectionLifecycleUseCase inspectionLifecycleUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInboundCompleted(InboundCompletedEvent event) {
        for (var line : event.lines()) {
            if (line.receivedQuantity() <= 0) {
                continue;
            }
            inspectionLifecycleUseCase.create(new CreateInspectionCommand(
                    InspectionSourceType.INBOUND,
                    line.lineId(),
                    null,
                    line.receivedQuantity(),
                    line.productId(),
                    null,
                    null,
                    event.warehouseId(),
                    line.expirationDate()
            ));
        }
    }
}