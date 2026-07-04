package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inspection.application.port.out.InspectionCreationFailurePort.InspectionCreationFailure;
import jakarta.persistence.*;
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

    private FailedInspectionEventEntity(Long inboundId, Long lineId, Long productId, Long warehouseId,
                                        int receivedQuantity, String manufacturerLotNumber,
                                        LocalDate expirationDate, String errorMessage) {
        this.inboundId = inboundId;
        this.lineId = lineId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.receivedQuantity = receivedQuantity;
        this.manufacturerLotNumber = manufacturerLotNumber;
        this.expirationDate = expirationDate;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
    }

    static FailedInspectionEventEntity from(InspectionCreationFailure failure) {
        return new FailedInspectionEventEntity(
                failure.inboundId(),
                failure.lineId(),
                failure.productId(),
                failure.warehouseId(),
                failure.receivedQuantity(),
                failure.manufacturerLotNumber(),
                failure.expirationDate(),
                failure.errorMessage()
        );
    }
}