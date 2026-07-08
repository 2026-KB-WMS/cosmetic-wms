package com.kb.cosmetic_wms.inspection.application.port.in;

import java.time.LocalDate;

public record RecordInspectionFailureCommand(
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
