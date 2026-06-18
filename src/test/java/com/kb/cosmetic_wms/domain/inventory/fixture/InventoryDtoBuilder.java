package com.kb.cosmetic_wms.domain.inventory.fixture;

import com.kb.cosmetic_wms.domain.inventory.dto.InventoryStatusChangeRequestDto;

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

    public InventoryStatusChangeRequestDto build() {
        return new InventoryStatusChangeRequestDto(quantity, referenceId, memberId);
    }

    public InventoryStatusChangeRequestDto buildWithoutRef() {
        return new InventoryStatusChangeRequestDto(quantity, null, memberId);
    }
}
