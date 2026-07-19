package com.kb.ordering.assignment.application.port.out.dto;

import java.math.BigDecimal;

public record WarehouseView(
        Long warehouseId,
        String warehouseName,
        String address,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
