package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import com.kb.cosmetic_wms.inspection.domain.model.FailedInspection;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "failed_inspection_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FailedInspectionEventEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failed_event_id")
    private Long id;

    @Column(name = "inbound_id", nullable = false)
    private Long inboundId;

    @Column(name = "line_id", nullable = false)
    private Long lineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    @Column(name = "manufacturer_lot_number", length = 100)
    private String manufacturerLotNumber;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

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

    static FailedInspectionEventEntity fromDomain(FailedInspection failed) {
        FailedInspectionEventEntity entity = new FailedInspectionEventEntity();
        entity.id = failed.getId();
        entity.inboundId = failed.getInboundId();
        entity.lineId = failed.getLineId();
        entity.productId = failed.getProductId();
        entity.warehouseId = failed.getWarehouseId();
        entity.receivedQuantity = failed.getReceivedQuantity();
        entity.manufacturerLotNumber = failed.getManufacturerLotNumber();
        entity.expirationDate = failed.getExpirationDate();
        entity.errorMessage = failed.getErrorMessage();
        entity.failedAt = failed.getFailedAt();
        entity.status = failed.getStatus();
        entity.retryCount = failed.getRetryCount();
        entity.resolvedAt = failed.getResolvedAt();
        return entity;
    }

    FailedInspection toDomain() {
        return FailedInspection.reconstitute(id, inboundId, lineId, productId, warehouseId, receivedQuantity,
                manufacturerLotNumber, expirationDate, errorMessage, failedAt, status, retryCount, resolvedAt);
    }
}
