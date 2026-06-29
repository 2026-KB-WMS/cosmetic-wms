package com.kb.cosmetic_wms.inventory.application.port.out;

public interface DockingSectionQueryPort {

    Long findDockingSectionId(Long warehouseId, Long productId);
}
