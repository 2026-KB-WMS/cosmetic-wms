package com.kb.cosmetic_wms.inbound.domain.model;

import java.time.LocalDateTime;

public record ReceiveLineData(
        int receivedQuantity,
        String manufacturerLotNumber,
        LocalDateTime manufacturingDate,
        LocalDateTime expirationDate
) {}
