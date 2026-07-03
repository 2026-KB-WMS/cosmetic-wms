package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.order.domain.event.OrderConfirmedEvent;
import jakarta.persistence.*;
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

    private FailedAssignmentEventEntity(Long orderId, Long storeId, String errorMessage) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
    }

    static FailedAssignmentEventEntity from(OrderConfirmedEvent event, String errorMessage) {
        return new FailedAssignmentEventEntity(event.orderId(), event.storeId(), errorMessage);
    }
}
