package com.kb.cosmetic_wms.lot.application.port.in;

public interface UpdateLotStatusUseCase {
    LotResult updateStatus(Long lotId, UpdateLotStatusCommand command);
}