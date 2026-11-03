package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;

import java.util.List;

public interface FindFailedInspectionUseCase {

    List<FailedInspectionResult> findByStatus(FailedEventStatus status);
}
