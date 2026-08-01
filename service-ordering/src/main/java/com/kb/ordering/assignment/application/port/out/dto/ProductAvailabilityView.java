package com.kb.ordering.assignment.application.port.out.dto;

import java.time.LocalDate;

public record ProductAvailabilityView(
        Long warehouseId,
        Long productId,
        int availableQuantity,
        LocalDate earliestExpiryDate
) {
}
