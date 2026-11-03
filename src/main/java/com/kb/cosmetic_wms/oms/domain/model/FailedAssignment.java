package com.kb.cosmetic_wms.oms.domain.model;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.exception.FailedAssignmentNotPendingException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FailedAssignment {

    private final Long id;
    private final Long orderId;
    private final Long storeId;
    private final LocalDateTime failedAt;

    private String errorMessage;
    private FailedEventStatus status;
    private int retryCount;
    private LocalDateTime resolvedAt;

    private FailedAssignment(Long id, Long orderId, Long storeId, String errorMessage, LocalDateTime failedAt,
                             FailedEventStatus status, int retryCount, LocalDateTime resolvedAt) {
        this.id = id;
        this.orderId = orderId;
        this.storeId = storeId;
        this.errorMessage = errorMessage;
        this.failedAt = failedAt;
        this.status = status;
        this.retryCount = retryCount;
        this.resolvedAt = resolvedAt;
    }

    public static FailedAssignment record(Long orderId, Long storeId, String errorMessage, LocalDateTime failedAt) {
        return new FailedAssignment(null, orderId, storeId, errorMessage, failedAt, FailedEventStatus.PENDING, 0, null);
    }

    public static FailedAssignment reconstitute(Long id, Long orderId, Long storeId, String errorMessage,
                                                LocalDateTime failedAt, FailedEventStatus status, int retryCount,
                                                LocalDateTime resolvedAt) {
        return new FailedAssignment(id, orderId, storeId, errorMessage, failedAt, status, retryCount, resolvedAt);
    }

    public void assertPending() {
        if (status != FailedEventStatus.PENDING) {
            throw new FailedAssignmentNotPendingException();
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
