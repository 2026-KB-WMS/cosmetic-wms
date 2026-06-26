package com.kb.cosmetic_wms.inventory.fixture;

import com.kb.cosmetic_wms.inventory.application.port.in.InventoryStatusChangeCommand;

public class InventoryDtoBuilder {

    private int quantity = 30;
    private Long referenceId = 1L;
    private Long memberId = 1L;

    public InventoryDtoBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InventoryDtoBuilder referenceId(Long referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public InventoryDtoBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public InventoryStatusChangeCommand build() {
        return new InventoryStatusChangeCommand(quantity, referenceId, memberId);
    }

    public InventoryStatusChangeCommand buildWithoutRef() {
        return new InventoryStatusChangeCommand(quantity, null, memberId);
    }
}