package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionCommand;
import com.kb.cosmetic_wms.inspection.application.port.in.CreateInspectionUseCase;
import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component("inspectionInboundCompletedEventAdapter")
@RequiredArgsConstructor
public class InboundCompletedEventAdapter {

    private final CreateInspectionUseCase createInspectionUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInboundCompleted(InboundCompletedEvent event) {
        for (var line : event.lines()) {
            if (line.receivedQuantity() <= 0) {
                continue;
            }
            createInspectionUseCase.create(new CreateInspectionCommand(
                    InspectionSourceType.INBOUND,
                    line.lineId(),
                    line.receivedQuantity(),
                    line.productId(),
                    event.inboundId(),
                    line.manufacturerLotNumber(),
                    event.warehouseId(),
                    line.expirationDate() != null ? line.expirationDate().toLocalDate() : null
            ));
        }
    }
}
