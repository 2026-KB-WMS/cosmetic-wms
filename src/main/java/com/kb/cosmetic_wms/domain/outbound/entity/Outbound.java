package com.kb.cosmetic_wms.domain.outbound.entity;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Outbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orders_id")
    private Long ordersId;

    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    private OutboundStatus outboundStatus;

    private LocalDateTime outboundDate;

    @OneToMany(mappedBy = "outbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OutboundItem> outboundItems = new ArrayList<>();

    private Outbound(Long ordersId, Long warehouseId, OutboundStatus outboundStatus) {
        this.ordersId = ordersId;
        this.warehouseId = warehouseId;
        this.outboundStatus = outboundStatus;
        this.outboundDate = null;
    }

    public static Outbound create(Long ordersId, Long warehouseId) {
        validateOrderId(ordersId);
        validateWarehouseId(warehouseId);

        return new Outbound(ordersId, warehouseId, OutboundStatus.PENDING);
    }

    /**
     * 출고 상세 항목 추가
     * <p>현장 작업이 시작되기 전인 PENDING(출고 대기) 상태에서만 품목 구성이 가능합니다.
     *
     * @param line 출고 라인 명세 (주문 품목, 재고 식별자, 지시 수량)
     * @return 생성된 OutboundItem 엔티티
     */
    public OutboundItem addItem(OutboundLine line) {
        if (this.outboundStatus != OutboundStatus.PENDING) {
            throw new IllegalStateException(OutboundConstants.INVALID_ADD_ITEM_STATUS_MESSAGE);
        }
        OutboundItem item = new OutboundItem(this, line);
        this.outboundItems.add(item);

        return item;
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

        boolean isAllPicked = this.outboundItems.stream()
                .allMatch(OutboundItem::isFullyPicked);

        if (!isAllPicked) {
            throw new IllegalStateException(OutboundConstants.INCOMPLETE_PICKING_MESSAGE);
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

    private static void validateOrderId(Long ordersId) {
        if (ordersId == null) {
            throw new IllegalArgumentException(OutboundConstants.ORDER_ID_REQUIRED_MESSAGE);
        }
    }

    private static void validateWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException(OutboundConstants.WAREHOUSE_REQUIRED_ID_MESSAGE);
        }
    }
}
