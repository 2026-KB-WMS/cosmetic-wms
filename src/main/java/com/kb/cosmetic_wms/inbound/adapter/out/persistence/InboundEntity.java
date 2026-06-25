package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inbound")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InboundEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InboundStatus inboundStatus;

    @Column(name = "inbound_date", nullable = false)
    private LocalDateTime inboundDate;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @OneToMany(mappedBy = "inbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundItemEntity> inboundItems = new ArrayList<>();

    private InboundEntity(Long id, InboundStatus inboundStatus, LocalDateTime inboundDate,
                          Long warehouseId, Long partnerId) {
        this.id = id;
        this.inboundStatus = inboundStatus;
        this.inboundDate = inboundDate;
        this.warehouseId = warehouseId;
        this.partnerId = partnerId;
    }

    static InboundEntity fromDomain(Inbound inbound) {
        InboundEntity entity = new InboundEntity(
                inbound.getId(), inbound.getInboundStatus(),
                inbound.getInboundDate(), inbound.getWarehouseId(), inbound.getPartnerId()
        );
        inbound.getInboundItems().forEach(item -> {
            InboundItemEntity itemEntity = InboundItemEntity.fromDomain(item);
            itemEntity.setInbound(entity);
            entity.inboundItems.add(itemEntity);
        });
        return entity;
    }

    Inbound toDomain() {
        List<com.kb.cosmetic_wms.inbound.domain.model.InboundItem> items = inboundItems.stream()
                .map(InboundItemEntity::toDomain)
                .toList();
        return Inbound.reconstitute(id, inboundStatus, inboundDate, warehouseId, partnerId, items);
    }
}
