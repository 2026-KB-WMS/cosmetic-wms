package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "outbound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OutboundEntity extends BaseEntity {

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
    private List<OutboundItemEntity> outboundItems = new ArrayList<>();

    private OutboundEntity(Long id, Long ordersId, Long warehouseId,
                           OutboundType outboundType, OutboundStatus outboundStatus,
                           LocalDateTime outboundDate) {
        this.id = id;
        this.ordersId = ordersId;
        this.warehouseId = warehouseId;
        this.outboundType = outboundType;
        this.outboundStatus = outboundStatus;
        this.outboundDate = outboundDate;
    }

    static OutboundEntity fromDomain(Outbound outbound) {
        OutboundEntity entity = new OutboundEntity(
                outbound.getId(), outbound.getOrdersId(), outbound.getWarehouseId(),
                outbound.getOutboundType(), outbound.getOutboundStatus(), outbound.getOutboundDate()
        );
        outbound.getOutboundItems().forEach(item -> {
            OutboundItemEntity itemEntity = OutboundItemEntity.fromDomain(item);
            itemEntity.setOutbound(entity);
            entity.outboundItems.add(itemEntity);
        });
        return entity;
    }

    Outbound toDomain() {
        List<OutboundItem> items = outboundItems.stream()
                .map(OutboundItemEntity::toDomain)
                .toList();
        return Outbound.reconstitute(id, ordersId, warehouseId, outboundType, outboundStatus, outboundDate, items);
    }
}