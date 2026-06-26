package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import com.kb.cosmetic_wms.outbound.domain.model.OutboundItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbound_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OutboundItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id", nullable = false, foreignKey = @ForeignKey(name = "fk_outbound_item_outbound"))
    private OutboundEntity outbound;

    @Column(name = "orders_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "target_quantity", nullable = false)
    private int targetQuantity;

    @Column(name = "picked_quantity", nullable = false)
    private int pickedQuantity;

    private OutboundItemEntity(Long id, Long orderItemId, Long inventoryId,
                               int targetQuantity, int pickedQuantity) {
        this.id = id;
        this.orderItemId = orderItemId;
        this.inventoryId = inventoryId;
        this.targetQuantity = targetQuantity;
        this.pickedQuantity = pickedQuantity;
    }

    static OutboundItemEntity fromDomain(OutboundItem item) {
        return new OutboundItemEntity(
                item.getId(), item.getOrderItemId(), item.getInventoryId(),
                item.getTargetQuantity(), item.getPickedQuantity()
        );
    }

    void setOutbound(OutboundEntity outbound) {
        this.outbound = outbound;
    }

    OutboundItem toDomain() {
        return OutboundItem.reconstitute(id, orderItemId, inventoryId, targetQuantity, pickedQuantity);
    }
}