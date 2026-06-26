package com.kb.cosmetic_wms.lot.application.port.in;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;

public record UpdateLotStatusCommand(LotStatus status) {
}