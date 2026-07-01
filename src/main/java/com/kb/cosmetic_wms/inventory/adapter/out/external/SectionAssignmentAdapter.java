package com.kb.cosmetic_wms.inventory.adapter.out.external;

import com.kb.cosmetic_wms.inventory.application.port.out.SectionAssignmentPort;
import com.kb.cosmetic_wms.storage.application.port.in.ApplyInspectionCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.ApplyInspectionCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.SectionAssignmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SectionAssignmentAdapter implements SectionAssignmentPort {

    private final ApplyInspectionCapacityUseCase applyInspectionCapacityUseCase;

    @Override
    public SectionAssignment assignSectionsForInspection(Long warehouseId, Long productId, int passedQty, int failedQty) {
        SectionAssignmentResult result = applyInspectionCapacityUseCase.applyInspectionCapacity(
                new ApplyInspectionCapacityCommand(warehouseId, productId, passedQty, failedQty));
        return new SectionAssignment(result.storageSectionId(), result.quarantineSectionId());
    }
}