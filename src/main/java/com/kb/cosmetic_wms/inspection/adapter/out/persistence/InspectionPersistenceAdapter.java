package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inspection.application.port.out.InspectionPort;
import com.kb.cosmetic_wms.inspection.domain.model.QualityInspection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InspectionPersistenceAdapter implements InspectionPort {

    private final InspectionJpaRepository inspectionJpaRepository;

    @Override
    public Optional<QualityInspection> findById(Long id) {
        return inspectionJpaRepository.findById(id).map(QualityInspectionEntity::toDomain);
    }

    @Override
    public Optional<QualityInspection> findByIdForUpdate(Long id) {
        return inspectionJpaRepository.findByIdForUpdate(id).map(QualityInspectionEntity::toDomain);
    }

    @Override
    public QualityInspection save(QualityInspection inspection) {
        return inspectionJpaRepository.save(QualityInspectionEntity.fromDomain(inspection)).toDomain();
    }
}