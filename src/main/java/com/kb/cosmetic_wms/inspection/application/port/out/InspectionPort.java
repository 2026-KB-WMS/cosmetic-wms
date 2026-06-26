package com.kb.cosmetic_wms.inspection.application.port.out;

import com.kb.cosmetic_wms.inspection.domain.model.Inspection;

import java.util.Optional;

public interface InspectionPort {

    Optional<Inspection> findById(Long id);

    Optional<Inspection> findByIdForUpdate(Long id);

    Inspection save(Inspection inspection);
}