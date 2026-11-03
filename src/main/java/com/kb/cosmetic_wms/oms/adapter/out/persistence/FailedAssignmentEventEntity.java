package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.oms.domain.model.FailedAssignment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_assignment_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FailedAssignmentEventEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failed_event_id")
    private Long id;

    @Column(name = "orders_id", nullable = false)
    private Long orderId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "failed_at", nullable = false)
    private LocalDateTime failedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FailedEventStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    static FailedAssignmentEventEntity fromDomain(FailedAssignment failed) {
        FailedAssignmentEventEntity entity = new FailedAssignmentEventEntity();
        entity.id = failed.getId();
        entity.orderId = failed.getOrderId();
        entity.storeId = failed.getStoreId();
        entity.errorMessage = failed.getErrorMessage();
        entity.failedAt = failed.getFailedAt();
        entity.status = failed.getStatus();
        entity.retryCount = failed.getRetryCount();
        entity.resolvedAt = failed.getResolvedAt();
        return entity;
    }

    FailedAssignment toDomain() {
        return FailedAssignment.reconstitute(id, orderId, storeId, errorMessage, failedAt, status, retryCount, resolvedAt);
    }
}
