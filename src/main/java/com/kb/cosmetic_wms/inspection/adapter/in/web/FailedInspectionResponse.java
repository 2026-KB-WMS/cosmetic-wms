package com.kb.cosmetic_wms.inspection.adapter.in.web;

import com.kb.cosmetic_wms.inspection.application.port.in.FailedInspectionResult;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FailedInspectionResponse(
        Long failedEventId,
        Long inboundId,
        Long lineId,
        Long productId,
        Long warehouseId,
        int receivedQuantity,
        String manufacturerLotNumber,
        LocalDate expirationDate,
        String errorMessage,
        LocalDateTime failedAt,
        FailedEventStatus status,
        int retryCount,
        LocalDateTime resolvedAt
) {
    public static FailedInspectionResponse from(FailedInspectionResult result) {
        return new FailedInspectionResponse(
                result.id(), result.inboundId(), result.lineId(), result.productId(), result.warehouseId(),
                result.receivedQuantity(), result.manufacturerLotNumber(), result.expirationDate(),
                result.errorMessage(), result.failedAt(), result.status(), result.retryCount(), result.resolvedAt());
    }
}
