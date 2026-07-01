package com.kb.cosmetic_wms.inventory.adapter.in.event;

import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.application.port.out.SectionAssignmentPort;
import com.kb.cosmetic_wms.inspection.domain.event.InspectionCompletedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.outbound.domain.event.OutboundStockReleaseRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class InventoryEventAdapter {

    private final ApplyInspectionResultUseCase applyInspectionResultUseCase;
    private final ManageInventoryStatusUseCase manageInventoryStatusUseCase;
    private final DeductInventoryForOutboundUseCase deductInventoryForOutboundUseCase;
    private final ReleaseInventoryForOutboundUseCase releaseInventoryForOutboundUseCase;
    private final SectionAssignmentPort sectionAssignmentPort;
    private final AuditorAware<Long> auditorProvider;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onInspectionCompleted(InspectionCompletedEvent event) {
        Long actorId = auditorProvider.getCurrentAuditor().orElseThrow();
        SectionAssignmentPort.SectionAssignment assignment = sectionAssignmentPort.assignSectionsForInspection(
                event.warehouseId(), event.productId(), event.passedQuantity(), event.failedQuantity());
        applyInspectionResultUseCase.applyInspectionResult(new InspectionResultCommand(
                event.productId(),
                event.lotId(),
                assignment.storageSectionId(),
                assignment.quarantineSectionId(),
                event.warehouseId(),
                event.passedQuantity(),
                event.failedQuantity(),
                event.inspectionId(),
                actorId,
                event.expiryDate()
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
