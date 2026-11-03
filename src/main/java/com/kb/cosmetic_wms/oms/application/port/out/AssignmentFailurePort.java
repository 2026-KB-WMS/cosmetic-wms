package com.kb.cosmetic_wms.oms.application.port.out;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;

import java.util.List;
import java.util.Optional;

public interface AssignmentFailurePort {

    FailedAssignment save(FailedAssignment failedAssignment);

    Optional<FailedAssignment> findByIdForUpdate(Long id);

    List<FailedAssignment> findByStatus(FailedEventStatus status);
}
