package com.kb.cosmetic_wms.oms.adapter.in.web;

import com.kb.cosmetic_wms.oms.application.port.in.FailedAssignmentResult;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;

import java.time.LocalDateTime;

public record FailedAssignmentResponse(
        Long failedEventId,
        Long orderId,
        Long storeId,
        String errorMessage,
        LocalDateTime failedAt,
        FailedEventStatus status,
        int retryCount,
        LocalDateTime resolvedAt
) {
    public static FailedAssignmentResponse from(FailedAssignmentResult result) {
        return new FailedAssignmentResponse(
                result.id(), result.orderId(), result.storeId(), result.errorMessage(),
                result.failedAt(), result.status(), result.retryCount(), result.resolvedAt());
    }
}
