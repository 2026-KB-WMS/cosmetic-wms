package com.kb.cosmetic_wms.putaway.adapter.in.web;

import com.kb.cosmetic_wms.putaway.application.port.in.PutawayOrderResult;
import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;

public record PutawayOrderDetailResponse(
        Long id,
        Long inspectionId,
        Long lotId,
        Long productId,
        Long warehouseId,
        Long sourceSectionId,
        Long targetSectionId,
        int quantity,
        boolean normalQuality,
        PutawayStatus status
) {
    public static PutawayOrderDetailResponse from(PutawayOrderResult result) {
        return new PutawayOrderDetailResponse(
                result.id(),
                result.inspectionId(),
                result.lotId(),
                result.productId(),
                result.warehouseId(),
                result.sourceSectionId(),
                result.targetSectionId(),
                result.quantity(),
                result.normalQuality(),
                result.status()
        );
    }
}
