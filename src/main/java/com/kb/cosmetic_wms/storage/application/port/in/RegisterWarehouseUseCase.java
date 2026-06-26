package com.kb.cosmetic_wms.storage.application.port.in;

public interface RegisterWarehouseUseCase {

    WarehouseResult register(RegisterWarehouseCommand command);
}