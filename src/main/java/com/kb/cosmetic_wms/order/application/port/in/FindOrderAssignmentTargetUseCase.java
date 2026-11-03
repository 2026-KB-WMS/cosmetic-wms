package com.kb.cosmetic_wms.order.application.port.in;

import java.util.Optional;

public interface FindOrderAssignmentTargetUseCase {

    Optional<OrderAssignmentTarget> findAssignmentTarget(Long orderId);
}
