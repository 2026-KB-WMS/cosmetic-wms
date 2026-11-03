package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.oms.application.port.out.AssignmentFailurePort;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AssignmentFailurePersistenceAdapter implements AssignmentFailurePort {

    private final FailedAssignmentEventJpaRepository failedAssignmentEventJpaRepository;

    @Override
    public FailedAssignment save(FailedAssignment failedAssignment) {
        return failedAssignmentEventJpaRepository
                .save(FailedAssignmentEventEntity.fromDomain(failedAssignment))
                .toDomain();
    }

    @Override
    public Optional<FailedAssignment> findByIdForUpdate(Long id) {
        return failedAssignmentEventJpaRepository.findByIdForUpdate(id)
                .map(FailedAssignmentEventEntity::toDomain);
    }

    @Override
    public List<FailedAssignment> findByStatus(FailedEventStatus status) {
        return failedAssignmentEventJpaRepository.findByStatusOrderByFailedAtAsc(status).stream()
                .map(FailedAssignmentEventEntity::toDomain)
                .toList();
    }
}
