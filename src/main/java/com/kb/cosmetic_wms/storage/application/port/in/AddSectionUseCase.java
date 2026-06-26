package com.kb.cosmetic_wms.storage.application.port.in;

public interface AddSectionUseCase {

    WarehouseResult addSection(Long warehouseId, AddSectionCommand command);
}