package com.kb.cosmetic_wms.inventory.application.port.in;

import java.util.List;

public interface FindInventoryUseCase {
    InventoryResult findById(Long inventoryId);
    List<InventoryResult> findByLotId(Long lotId);
    List<InventoryResult> findByProductId(Long productId);
}
