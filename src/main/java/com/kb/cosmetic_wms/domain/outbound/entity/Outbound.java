package com.kb.cosmetic_wms.domain.outbound.entity;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.exception.*;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outbound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Outbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_id")
    private Long id;

    @Column(name = "orders_id", nullable = false)
    private Long ordersId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbound_type", nullable = false, length = 20)
    private OutboundType outboundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "outbound_status", nullable = false, length = 20)
    private OutboundStatus outboundStatus;

    @Column(name = "outbound_date")
    private LocalDateTime outboundDate;

    @OneToMany(mappedBy = "outbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OutboundItem> outboundItems = new ArrayList<>();

    private Outbound(Long ordersId, Long warehouseId, OutboundType outboundType) {
        this.ordersId = ordersId;
        this.warehouseId = warehouseId;
        this.outboundType = outboundType;
        this.outboundStatus = OutboundStatus.PENDING;
        this.outboundDate = null;
    }

    public static Outbound create(Long ordersId, Long warehouseId, OutboundType outboundType) {
        if (ordersId == null) throw new OutboundOrderIdRequiredException();
        if (warehouseId == null) throw new OutboundWarehouseRequiredException();
        if (outboundType == null) throw new OutboundTypeRequiredException();
        return new Outbound(ordersId, warehouseId, outboundType);
    }

    public OutboundItem addItem(OutboundLine line) {
        if (this.outboundStatus != OutboundStatus.PENDING) {
            throw new IllegalStateException(OutboundConstants.INVALID_ADD_ITEM_STATUS_MESSAGE);
        }
        OutboundItem item = new OutboundItem(this, line);
        this.outboundItems.add(item);
        return item;
    }

    /**
     * 재고 할당 (PENDING → ALLOCATED)
     */
    public void allocate() {
        if (this.outboundStatus != OutboundStatus.PENDING) {
            throw new OutboundAllocateNotAllowedException();
        }
        this.outboundStatus = OutboundStatus.ALLOCATED;
    }

    /**
     * 출고 작업 시작 (ALLOCATED → PROCESSING)
     */
    public void startProcessing() {
        if (this.outboundStatus != OutboundStatus.ALLOCATED) {
            throw new OutboundProcessingNotAllowedException();
        }
        this.outboundStatus = OutboundStatus.PROCESSING;
    }

    /**
     * 출하 완료 (PROCESSING → SHIPPED)
     */
    public void ship() {
        if (this.outboundStatus != OutboundStatus.PROCESSING) {
            throw new OutboundShipNotAllowedException();
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
     * 출고 취소 (PENDING 또는 ALLOCATED → CANCELED)
     */
    public void cancel() {
        if (this.outboundStatus != OutboundStatus.PENDING
                && this.outboundStatus != OutboundStatus.ALLOCATED) {
            throw new OutboundCancelNotAllowedException();
        }
        this.outboundStatus = OutboundStatus.CANCELED;
    }
}