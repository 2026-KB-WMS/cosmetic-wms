package com.kb.cosmetic_wms.inventory.application.port.in;

import java.time.LocalDate;

public record InboundPutawayCommand(
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int quantity,
        Long inboundId,
        Long memberId,
        LocalDate expiryDate
) {
}