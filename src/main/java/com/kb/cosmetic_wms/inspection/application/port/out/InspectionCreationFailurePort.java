package com.kb.cosmetic_wms.inspection.application.port.out;

import java.time.LocalDate;

public interface InspectionCreationFailurePort {

    void save(InspectionCreationFailure failure);

    record InspectionCreationFailure(
            Long inboundId,
            Long lineId,
            Long productId,
            Long warehouseId,
            int receivedQuantity,
            String manufacturerLotNumber,
            LocalDate expirationDate,
            String errorMessage
    ) {
    }
}
