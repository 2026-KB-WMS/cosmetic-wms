package com.kb.cosmetic_wms.inventory.adapter.in.event;

import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.application.port.out.DockingSectionQueryPort;
import com.kb.cosmetic_wms.inspection.domain.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundStockReleaseRequestedEvent;
import com.kb.cosmetic_wms.putaway.domain.event.PutawayCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventAdapter {

    private final ApplyInspectionResultUseCase applyInspectionResultUseCase;
    private final CompletePutawayInventoryUseCase completePutawayInventoryUseCase;
    private final ManageInventoryStatusUseCase manageInventoryStatusUseCase;
    private final DeductInventoryForOutboundUseCase deductInventoryForOutboundUseCase;
    private final ReleaseInventoryForOutboundUseCase releaseInventoryForOutboundUseCase;
    private final DockingSectionQueryPort dockingSectionQueryPort;
    private final AuditorAware<Long> auditorProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInspectionCompleted(InspectionCompletedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        Long sectionId = dockingSectionQueryPort.findDockingSectionId(event.warehouseId(), event.productId());
        applyInspectionResultUseCase.applyInspectionResult(new InspectionResultCommand(
                event.productId(),
                event.lotId(),
                sectionId,
                event.warehouseId(),
                event.passedQuantity(),
                event.failedQuantity(),
                event.inspectionId(),
                actorId,
                event.expiryDate()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onPutawayCompleted(PutawayCompletedEvent event) {
        completePutawayInventoryUseCase.completePutaway(new CompletePutawayInventoryCommand(
                event.putawayOrderId(),
                event.lotId(),
                event.warehouseId(),
                event.sourceSectionId(),
                event.targetSectionId(),
                event.normalQuality(),
                event.memberId()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundAllocated(OutboundAllocatedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        for (OutboundAllocatedEvent.ItemSnapshot item : event.items()) {
            manageInventoryStatusUseCase.allocate(item.inventoryId(),
                    new InventoryStatusChangeCommand(item.targetQuantity(), event.outboundId(), actorId));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundShipped(OutboundShippedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        deductInventoryForOutboundUseCase.deductForOutbound(event.outboundId(), actorId);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOutboundStockReleaseRequested(OutboundStockReleaseRequestedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        releaseInventoryForOutboundUseCase.releaseForOutbound(event.outboundId(), actorId);
    }
}
