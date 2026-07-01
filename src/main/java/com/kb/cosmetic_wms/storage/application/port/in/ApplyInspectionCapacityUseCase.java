package com.kb.cosmetic_wms.storage.application.port.in;

public interface ApplyInspectionCapacityUseCase {
    SectionAssignmentResult applyInspectionCapacity(ApplyInspectionCapacityCommand command);
}