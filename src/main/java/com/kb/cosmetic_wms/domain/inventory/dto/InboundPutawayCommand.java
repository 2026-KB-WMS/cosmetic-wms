package com.kb.cosmetic_wms.domain.inventory.dto;

public record InboundPutawayCommand(
        Long productId,
        Long lotId,
        Long sectionId,
        Long warehouseId,
        int quantity,
        Long inboundId,
        Long memberId
) {}
