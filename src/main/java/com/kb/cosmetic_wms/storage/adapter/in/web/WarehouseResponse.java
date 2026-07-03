package com.kb.cosmetic_wms.storage.adapter.in.web;

import com.kb.cosmetic_wms.storage.application.port.in.WarehouseResult;

import java.math.BigDecimal;
import java.util.List;

public record WarehouseResponse(
        Long warehouseId,
        String warehouseName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String targetTemp,
        int capacity,
        List<SectionResponse> sections
) {
    public static WarehouseResponse from(WarehouseResult result) {
        return new WarehouseResponse(
                result.warehouseId(),
                result.warehouseName(),
                result.address(),
                result.latitude(),
                result.longitude(),
                result.targetTemp(),
                result.capacity(),
                result.sections().stream().map(SectionResponse::from).toList()
        );
    }
}
