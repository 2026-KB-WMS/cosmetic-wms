package com.kb.cosmetic_wms.inventory.application.port.in;

import java.time.LocalDate;

public record ProductAvailabilitySlice(
        Long warehouseId,
        Long productId,
        int availableQuantity,
        LocalDate earliestExpiryDate
) {
}
