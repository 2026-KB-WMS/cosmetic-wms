package com.kb.cosmetic_wms.inventory.application.port.in;

public interface ManageInventoryStatusUseCase {
    InventoryResult allocate(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult unallocate(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult startMoving(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult finishMoving(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult startInspecting(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult restoreToNormalQuality(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult holdForQualityIssue(Long inventoryId, InventoryStatusChangeCommand command);
    InventoryResult scheduleForDiscard(Long inventoryId, InventoryStatusChangeCommand command);
}
