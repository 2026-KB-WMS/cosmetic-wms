package com.kb.cosmetic_wms.inspection.application.port.in;

public interface InspectionLifecycleUseCase {

    InspectionResult start(Long inspectionId, Long inspectorId);

    InspectionResult complete(Long inspectionId, int passedQty, int failedQty, String defectReason);
}