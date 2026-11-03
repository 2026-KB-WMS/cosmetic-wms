package com.kb.cosmetic_wms.inspection.domain.model;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.exception.FailedInspectionNotPendingException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class FailedInspection {

    private final Long id;
    private final Long inboundId;
    private final Long lineId;
    private final Long productId;
    private final Long warehouseId;
    private final int receivedQuantity;
    private final String manufacturerLotNumber;
    private final LocalDate expirationDate;
    private final LocalDateTime failedAt;

    private String errorMessage;
    private FailedEventStatus status;
    private int retryCount;
    private LocalDateTime resolvedAt;

    private FailedInspection(Long id, Long inboundId, Long lineId, Long productId, Long warehouseId,
                             int receivedQuantity, String manufacturerLotNumber, LocalDate expirationDate,
                             String errorMessage, LocalDateTime failedAt, FailedEventStatus status,
                             int retryCount, LocalDateTime resolvedAt) {
        this.id = id;
        this.inboundId = inboundId;
        this.lineId = lineId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.receivedQuantity = receivedQuantity;
        this.manufacturerLotNumber = manufacturerLotNumber;
        this.expirationDate = expirationDate;
        this.errorMessage = errorMessage;
        this.failedAt = failedAt;
        this.status = status;
        this.retryCount = retryCount;
        this.resolvedAt = resolvedAt;
    }

    public static FailedInspection record(Long inboundId, Long lineId, Long productId, Long warehouseId,
                                          int receivedQuantity, String manufacturerLotNumber,
                                          LocalDate expirationDate, String errorMessage, LocalDateTime failedAt) {
        return new FailedInspection(null, inboundId, lineId, productId, warehouseId, receivedQuantity,
                manufacturerLotNumber, expirationDate, errorMessage, failedAt, FailedEventStatus.PENDING, 0, null);
    }

    public static FailedInspection reconstitute(Long id, Long inboundId, Long lineId, Long productId,
                                                Long warehouseId, int receivedQuantity,
                                                String manufacturerLotNumber, LocalDate expirationDate,
                                                String errorMessage, LocalDateTime failedAt,
                                                FailedEventStatus status, int retryCount,
                                                LocalDateTime resolvedAt) {
        return new FailedInspection(id, inboundId, lineId, productId, warehouseId, receivedQuantity,
                manufacturerLotNumber, expirationDate, errorMessage, failedAt, status, retryCount, resolvedAt);
    }

    public void assertPending() {
        if (status != FailedEventStatus.PENDING) {
            throw new FailedInspectionNotPendingException();
        }
    }

    public void markResolvedByRetry(LocalDateTime resolvedAt) {
        assertPending();
        this.retryCount++;
        this.status = FailedEventStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
    }

    public void recordRetryFailure(String errorMessage) {
        assertPending();
        this.retryCount++;
        this.errorMessage = errorMessage;
    }

    public void discard(LocalDateTime discardedAt) {
        assertPending();
        this.status = FailedEventStatus.DISCARDED;
        this.resolvedAt = discardedAt;
    }
}
