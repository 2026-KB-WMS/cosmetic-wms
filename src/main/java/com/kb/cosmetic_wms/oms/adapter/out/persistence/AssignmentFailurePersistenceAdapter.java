package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentFailurePersistenceAdapter implements AssignmentFailurePort {

    private final FailedAssignmentEventJpaRepository failedAssignmentEventJpaRepository;

    @Override
    public void save(Long orderId, Long storeId, String errorMessage) {
        failedAssignmentEventJpaRepository.save(FailedAssignmentEventEntity.of(orderId, storeId, errorMessage));
    }
}
