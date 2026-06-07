package com.kb.cosmetic_wms.domain.outbound.entity;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Outbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    private OutboundStatus outboundStatus;

    private LocalDateTime outboundDate;

    private Outbound(Long orderId, Long warehouseId, OutboundStatus outboundStatus) {
        this.orderId = orderId;
        this.warehouseId = warehouseId;
        this.outboundStatus = outboundStatus;
        this.outboundDate = null;
    }

    public static Outbound create(Long orderId, Long warehouseId) {
        validateOrderId(orderId);
        validateWarehouseId(warehouseId);

        return new Outbound(orderId, warehouseId, OutboundStatus.PENDING);
    }

    /**
     * 피킹 작업 시작
     *
     * <p>PENDING 상태에서만 전이 가능 (PENDING -> PICKING)
     */
    public void startPicking() {
        if (this.outboundStatus != OutboundStatus.PENDING) {
            throw new IllegalStateException(OutboundConstants.INVALID_START_PICKING_MESSAGE);
        }
        this.outboundStatus = OutboundStatus.PICKING;
    }

    /**
     * 출고 완료 (배송 출발)
     *
     * <p>PICKING 상태에서만 완료 가능 (PICKING -> SHIPPED)
     */
    public void ship() {
        if (this.outboundStatus != OutboundStatus.PICKING) {
            throw new IllegalStateException(OutboundConstants.INVALID_SHIP_MESSAGE);
        }
        this.outboundStatus = OutboundStatus.SHIPPED;
        this.outboundDate = LocalDateTime.now();
    }

    /**
     * 출고 전표 취소
     *
     * <p>현장 작업(PICKING)이 시작되기 전인 PENDING 상태에서만 취소 가능하며,
     * 한 번 취소되면 다른 상태로의 전이가 불가능한 종단 상태가 됨
     */
    public void cancel() {
        if (this.outboundStatus != OutboundStatus.PENDING) {
            throw new IllegalStateException(OutboundConstants.INVALID_CANCEL_MESSAGE);
        }
        this.outboundStatus = OutboundStatus.CANCELED;
    }

    private static void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException(OutboundConstants.ORDER_ID_REQUIRED_MESSAGE);
        }
    }

    private static void validateWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException(OutboundConstants.WAREHOUSE_REQUIRED_ID_MESSAGE);
        }
    }
}
