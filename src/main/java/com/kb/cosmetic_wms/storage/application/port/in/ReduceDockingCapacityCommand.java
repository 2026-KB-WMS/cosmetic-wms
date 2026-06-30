package com.kb.cosmetic_wms.storage.application.port.in;

import java.util.List;

public record ReduceDockingCapacityCommand(
        Long warehouseId,
        List<LineItem> lines
) {
    public record LineItem(Long productId, int quantity) {}
}
