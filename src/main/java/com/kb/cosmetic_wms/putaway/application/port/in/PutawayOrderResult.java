package com.kb.cosmetic_wms.putaway.application.port.in;

import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;

public record PutawayOrderResult(
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
    public static PutawayOrderResult from(PutawayOrder order) {
        return new PutawayOrderResult(
                order.getId(),
                order.getInspectionId(),
                order.getLotId(),
                order.getProductId(),
                order.getWarehouseId(),
                order.getSourceSectionId(),
                order.getTargetSectionId(),
                order.getQuantity(),
                order.isNormalQuality(),
                order.getStatus()
        );
    }
}
