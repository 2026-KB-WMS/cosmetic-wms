package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InspectionCreationFailurePersistenceAdapter implements InspectionCreationFailurePort {

    private final FailedInspectionEventJpaRepository failedInspectionEventJpaRepository;

    @Override
    public FailedInspection save(FailedInspection failedInspection) {
        return failedInspectionEventJpaRepository
                .save(FailedInspectionEventEntity.fromDomain(failedInspection))
                .toDomain();
    }

    @Override
    public Optional<FailedInspection> findByIdForUpdate(Long id) {
        return failedInspectionEventJpaRepository.findByIdForUpdate(id)
                .map(FailedInspectionEventEntity::toDomain);
    }

    @Override
    public List<FailedInspection> findByStatus(FailedEventStatus status) {
        return failedInspectionEventJpaRepository.findByStatusOrderByFailedAtAsc(status).stream()
                .map(FailedInspectionEventEntity::toDomain)
                .toList();
    }
}
