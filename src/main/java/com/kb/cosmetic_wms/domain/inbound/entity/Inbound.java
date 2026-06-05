package com.kb.cosmetic_wms.domain.inbound.entity;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InboundStatus inboundStatus;

    private LocalDateTime inboundDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    private Partner partner;

    private Inbound(LocalDateTime inboundDate, Warehouse warehouse, Partner partner) {
        this.inboundDate = inboundDate;
        this.warehouse = warehouse;
        this.partner = partner;
        this.inboundStatus = InboundStatus.SCHEDULED;
    }

    public static Inbound create(LocalDateTime inboundDate, Warehouse warehouse, Partner partner) {
        validateInboundDate(inboundDate);
        validateRequiredFields(warehouse, partner);

        return new Inbound(inboundDate, warehouse, partner);
    }

    // 입고 작업 시작 (SCHEDULED -> IN_PROGRESS)
    public void startExecution() {
        if (this.inboundStatus != InboundStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_START_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }
        this.inboundStatus = InboundStatus.IN_PROGRESS;
    }

    // 실물 적재 완료 (IN_PROGRESS -> COMPLETED)
    public void completeExecution() {
        if (this.inboundStatus != InboundStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_COMPLETE_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }
        this.inboundStatus = InboundStatus.COMPLETED;
    }

    // 입고 계획 취소 (SCHEDULED -> CANCELED)
    public void cancel() {
        if (this.inboundStatus != InboundStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_CANCEL_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }
        this.inboundStatus = InboundStatus.CANCELED;
    }

    private static void validateInboundDate(LocalDateTime inboundDate) {
        if (inboundDate == null) {
            throw new IllegalArgumentException(InboundConstants.DATE_REQUIRED_MESSAGE);
        }
        if (inboundDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(InboundConstants.PAST_INBOUND_DATE_MESSAGE);
        }
    }

    private static void validateRequiredFields(Warehouse warehouse, Partner partner) {
        if (warehouse == null) {
            throw new IllegalArgumentException(InboundConstants.WAREHOUSE_REQUIRED_MESSAGE);
        }
        if (partner == null) {
            throw new IllegalArgumentException(InboundConstants.PARTNER_REQUIRED_MESSAGE);
        }
    }
}
