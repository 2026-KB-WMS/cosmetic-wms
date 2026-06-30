package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InspectionCreationFailurePersistenceAdapter implements InspectionCreationFailurePort {

    private final FailedInspectionEventJpaRepository failedInspectionEventJpaRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(InboundCompletedEvent event, InboundCompletedEvent.LineSnapshot line, String errorMessage) {
        failedInspectionEventJpaRepository.save(FailedInspectionEventEntity.from(event, line, errorMessage));
    }
}
