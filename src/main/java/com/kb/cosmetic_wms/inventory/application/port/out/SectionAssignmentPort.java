package com.kb.cosmetic_wms.inventory.application.port.out;

public interface SectionAssignmentPort {
    record SectionAssignment(Long storageSectionId, Long quarantineSectionId) {}
    SectionAssignment assignSectionsForInspection(Long warehouseId, Long productId, int passedQty, int failedQty);
}
