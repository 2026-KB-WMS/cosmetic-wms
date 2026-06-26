package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.domain.model.Inspection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InspectionPersistenceAdapter implements InspectionPort {

    private final InspectionJpaRepository inspectionJpaRepository;

    @Override
    public Optional<Inspection> findById(Long id) {
        return inspectionJpaRepository.findById(id).map(InspectionEntity::toDomain);
    }

    @Override
    public Optional<Inspection> findByIdForUpdate(Long id) {
        return inspectionJpaRepository.findByIdForUpdate(id).map(InspectionEntity::toDomain);
    }

    @Override
    public Inspection save(Inspection inspection) {
        return inspectionJpaRepository.save(InspectionEntity.fromDomain(inspection)).toDomain();
    }
}