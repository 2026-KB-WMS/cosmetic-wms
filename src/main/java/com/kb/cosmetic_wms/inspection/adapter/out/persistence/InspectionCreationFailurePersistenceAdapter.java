package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InspectionCreationFailurePersistenceAdapter implements InspectionCreationFailurePort {

    private final FailedInspectionEventJpaRepository failedInspectionEventJpaRepository;

    @Override
    public void save(InspectionCreationFailure failure) {
        failedInspectionEventJpaRepository.save(FailedInspectionEventEntity.from(failure));
    }
}
