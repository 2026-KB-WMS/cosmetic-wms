package com.kb.cosmetic_wms.inventory.application.port.in;

public record InventoryStatusChangeCommand(int quantity, Long referenceId, Long memberId) {
}
