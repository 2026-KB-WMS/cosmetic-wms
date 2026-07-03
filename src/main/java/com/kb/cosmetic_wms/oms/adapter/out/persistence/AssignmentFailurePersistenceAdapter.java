package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentFailurePersistenceAdapter implements AssignmentFailurePort {

    private final FailedAssignmentEventJpaRepository failedAssignmentEventJpaRepository;

    @Override
    public void save(OrderConfirmedEvent event, String errorMessage) {
        failedAssignmentEventJpaRepository.save(FailedAssignmentEventEntity.from(event, errorMessage));
    }
}
