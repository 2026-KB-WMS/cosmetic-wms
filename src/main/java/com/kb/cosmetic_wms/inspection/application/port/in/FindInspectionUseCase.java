package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.InspectionSourceType;

import java.util.Optional;

public interface FindInspectionUseCase {

    InspectionResult findById(Long inspectionId);

    Optional<InspectionResult> findBySource(InspectionSourceType sourceType, Long sourceId);
}