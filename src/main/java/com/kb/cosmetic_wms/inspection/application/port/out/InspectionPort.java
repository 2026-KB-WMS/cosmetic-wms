package com.kb.cosmetic_wms.inspection.application.port.out;

import com.kb.cosmetic_wms.inspection.domain.model.QualityInspection;

import java.util.Optional;

public interface InspectionPort {

    Optional<QualityInspection> findById(Long id);

    Optional<QualityInspection> findByIdForUpdate(Long id);

    QualityInspection save(QualityInspection inspection);
}