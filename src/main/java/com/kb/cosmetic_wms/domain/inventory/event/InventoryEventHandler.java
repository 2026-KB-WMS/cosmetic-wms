package com.kb.cosmetic_wms.domain.inventory.event;

import com.kb.cosmetic_wms.domain.inventory.dto.InspectionResultCommand;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventHandler {

    private final InventoryService inventoryService;
    private final AuditorAware<Long> auditorProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInspectionCompleted(InspectionCompletedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        inventoryService.applyInspectionResult(new InspectionResultCommand(
                event.productId(),
                event.lotId(),
                event.sectionId(),
                event.warehouseId(),
                event.passedQuantity(),
                event.failedQuantity(),
                event.inspectionId(),
                actorId
        ));
    }
}