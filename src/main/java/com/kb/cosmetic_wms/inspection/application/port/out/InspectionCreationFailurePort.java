package com.kb.cosmetic_wms.inspection.application.port.out;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;

import java.util.List;
import java.util.Optional;

public interface InspectionCreationFailurePort {

    FailedInspection save(FailedInspection failedInspection);

    Optional<FailedInspection> findByIdForUpdate(Long id);

    List<FailedInspection> findByStatus(FailedEventStatus status);
}
