package com.kb.cosmetic_wms.domain.inbound.event;

import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;

import java.util.List;

public record InboundCompletedEvent(
        Long inboundId,
        Long warehouseId,
        List<ItemSnapshot> items
) {

    public record ItemSnapshot(
            Long inboundItemId,
            Long productId,
            Long lotId,
            Long sectionId,
            int quantity,
            InspectionStatus inspectionStatus
    ) {
    }

    public static InboundCompletedEvent from(Inbound inbound) {
        List<ItemSnapshot> snapshots = inbound.getInboundItems().stream()
                .map(item -> new ItemSnapshot(
                        item.getId(),
                        item.getProductId(),
                        item.getLotId(),
                        item.getSectionId(),
                        item.getQuantity(),
                        item.getInspectionStatus()
                ))
                .toList();
        return new InboundCompletedEvent(inbound.getId(), inbound.getWarehouseId(), snapshots);
    }
}
