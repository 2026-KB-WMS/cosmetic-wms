package com.kb.cosmetic_wms.oms.application.port.in;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;

import java.time.LocalDateTime;

public record FailedAssignmentResult(
        Long id,
        Long orderId,
        Long storeId,
        String errorMessage,
        LocalDateTime failedAt,
        FailedEventStatus status,
        int retryCount,
        LocalDateTime resolvedAt
) {
    public static FailedAssignmentResult from(FailedAssignment failed) {
        return new FailedAssignmentResult(
                failed.getId(), failed.getOrderId(), failed.getStoreId(), failed.getErrorMessage(),
                failed.getFailedAt(), failed.getStatus(), failed.getRetryCount(), failed.getResolvedAt());
    }
}
