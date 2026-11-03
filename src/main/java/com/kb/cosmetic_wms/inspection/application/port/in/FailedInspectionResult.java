package com.kb.cosmetic_wms.inspection.application.port.in;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FailedInspectionResult(
        Long id,
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
    public static FailedInspectionResult from(FailedInspection failed) {
        return new FailedInspectionResult(
                failed.getId(),
                failed.getInboundId(),
                failed.getLineId(),
                failed.getProductId(),
                failed.getWarehouseId(),
                failed.getReceivedQuantity(),
                failed.getManufacturerLotNumber(),
                failed.getExpirationDate(),
                failed.getErrorMessage(),
                failed.getFailedAt(),
                failed.getStatus(),
                failed.getRetryCount(),
                failed.getResolvedAt()
        );
    }
}
