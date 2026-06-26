package com.kb.cosmetic_wms.lot.application.port.in;

public interface RegisterLotUseCase {
    LotResult register(RegisterLotCommand command);
}