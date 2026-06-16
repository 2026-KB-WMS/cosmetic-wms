package com.kb.cosmetic_wms.domain.storage.dto;

import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;

import java.util.List;

public record WarehouseResponseDto(
        Long id,
        String warehouseName,
        String address,
        String targetTemp,
        int capacity,
        List<SectionResponseDto> sections
) {
    public static WarehouseResponseDto from(Warehouse warehouse) {
        return new WarehouseResponseDto(
                warehouse.getId(),
                warehouse.getWarehouseName(),
                warehouse.getAddress(),
                warehouse.getTargetTemp(),
                warehouse.getCapacity(),
                warehouse.getSections().stream()
                        .map(SectionResponseDto::from)
                        .toList()
        );
    }
}
