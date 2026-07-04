package com.kb.cosmetic_wms.outbound.application.port.out;

public interface InventoryAllocationPort {

    void allocate(Long inventoryId, int quantity, Long outboundId, Long memberId);
}
