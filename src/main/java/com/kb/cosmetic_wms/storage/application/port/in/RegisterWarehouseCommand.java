package com.kb.cosmetic_wms.storage.application.port.in;

public record RegisterWarehouseCommand(
        String warehouseName,
        String address,
        String targetTemp,
        int capacity
) {
}