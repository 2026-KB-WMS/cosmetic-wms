package com.kb.cosmetic_wms.inventory.application.port.in;

import java.util.List;

public interface FindFefoInventoryUseCase {
    List<FefoInventorySlice> findAvailableForFefo(Long productId, Long warehouseId);
}