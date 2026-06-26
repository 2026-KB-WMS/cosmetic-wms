package com.kb.cosmetic_wms.inspection.application.port.in;

public interface FindInspectionUseCase {

    InspectionResult findById(Long inspectionId);
}