package com.kb.cosmetic_wms.oms.application.port.in;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;

import java.util.List;

public interface FindFailedAssignmentUseCase {

    List<FailedAssignmentResult> findByStatus(FailedEventStatus status);
}
