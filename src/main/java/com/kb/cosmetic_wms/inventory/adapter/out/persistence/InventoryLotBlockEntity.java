package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_lot_block")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InventoryLotBlockEntity extends BaseEntity {

    @Id
    @Column(name = "lot_id")
    private Long lotId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    static InventoryLotBlockEntity of(Long lotId, String status) {
        InventoryLotBlockEntity entity = new InventoryLotBlockEntity();
        entity.lotId = lotId;
        entity.status = status;
        return entity;
    }

    void changeStatus(String status) {
        this.status = status;
    }
}
