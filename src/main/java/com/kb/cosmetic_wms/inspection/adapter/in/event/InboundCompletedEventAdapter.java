package com.kb.cosmetic_wms.inspection.adapter.in.event;

import com.kb.cosmetic_wms.global.event.InboundCompletedEvent;
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
        for (var item : event.items()) {
            inspectionLifecycleUseCase.create(new CreateInspectionCommand(
                    InspectionSourceType.INBOUND,
                    item.inboundItemId(),
                    null,
                    item.quantity(),
                    item.productId(),
                    item.lotId(),
                    item.sectionId(),
                    event.warehouseId(),
                    item.expiryDate()
            ));
        }
    }
}
