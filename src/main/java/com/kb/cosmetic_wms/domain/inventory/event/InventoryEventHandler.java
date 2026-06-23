package com.kb.cosmetic_wms.domain.inventory.event;

import com.kb.cosmetic_wms.domain.inventory.dto.InspectionResultCommand;
import com.kb.cosmetic_wms.domain.inventory.dto.InventoryStatusChangeRequestDto;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import com.kb.cosmetic_wms.global.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.global.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.global.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.global.event.OutboundStockReleaseRequestedEvent;
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundAllocated(OutboundAllocatedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        for (OutboundAllocatedEvent.ItemSnapshot item : event.items()) {
            inventoryService.allocate(item.inventoryId(),
                    new InventoryStatusChangeRequestDto(item.targetQuantity(), event.outboundId(), actorId));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundShipped(OutboundShippedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        inventoryService.deductForOutbound(event.outboundId(), actorId);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundStockReleaseRequested(OutboundStockReleaseRequestedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        inventoryService.releaseForOutbound(event.outboundId(), actorId);
    }
}