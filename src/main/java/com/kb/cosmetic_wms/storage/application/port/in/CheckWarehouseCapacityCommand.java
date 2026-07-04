package com.kb.cosmetic_wms.storage.application.port.in;

import java.util.List;

public record CheckWarehouseCapacityCommand(
        Long warehouseId,
        List<ProductQuantity> items
) {
    public record ProductQuantity(Long productId, int quantity) {
    }
}
