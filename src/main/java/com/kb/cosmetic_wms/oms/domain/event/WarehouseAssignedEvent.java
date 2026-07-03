package com.kb.cosmetic_wms.oms.domain.event;

import java.util.List;

public record WarehouseAssignedEvent(
        Long orderId,
        Long warehouseId,
        List<ItemSnapshot> items
) {
    public record ItemSnapshot(Long orderItemId, Long productId, int quantity) {}
}
