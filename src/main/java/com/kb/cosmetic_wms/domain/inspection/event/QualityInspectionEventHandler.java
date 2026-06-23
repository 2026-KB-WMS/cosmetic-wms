package com.kb.cosmetic_wms.domain.inspection.event;

import com.kb.cosmetic_wms.global.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.domain.inspection.service.InspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class QualityInspectionEventHandler {

    private final InspectionService inspectionService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInboundCompleted(InboundCompletedEvent event) {
        for (var item : event.items()) {
            inspectionService.createInboundInspection(
                    item.inboundItemId(),
                    item.productId(),
                    item.lotId(),
                    item.sectionId(),
                    event.warehouseId(),
                    item.quantity()
            );
        }
    }
}
