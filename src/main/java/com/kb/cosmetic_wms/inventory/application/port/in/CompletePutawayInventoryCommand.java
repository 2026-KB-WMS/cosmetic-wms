package com.kb.cosmetic_wms.inventory.application.port.in;

public record CompletePutawayInventoryCommand(
        Long putawayOrderId,
        Long lotId,
        Long warehouseId,
        Long sourceSectionId,
        Long targetSectionId,
        boolean normalQuality,
        Long memberId
) {}
