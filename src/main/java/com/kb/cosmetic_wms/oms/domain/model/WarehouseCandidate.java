package com.kb.cosmetic_wms.oms.domain.model;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
import com.kb.cosmetic_wms.oms.domain.exception.OmsValidationException;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Getter
public class WarehouseCandidate {

    private final Long warehouseId;
    private final GeoCoordinate coordinate;
    private final Map<Long, ProductStock> stocksByProductId;

    private WarehouseCandidate(Long warehouseId, GeoCoordinate coordinate,
                               Map<Long, ProductStock> stocksByProductId) {
        this.warehouseId = warehouseId;
        this.coordinate = coordinate;
        this.stocksByProductId = Map.copyOf(stocksByProductId);
    }

    public static WarehouseCandidate of(Long warehouseId, GeoCoordinate coordinate,
                                        Map<Long, ProductStock> stocksByProductId) {
        if (warehouseId == null || coordinate == null) {
            throw new OmsValidationException(OmsErrorCode.INVALID_WAREHOUSE_CANDIDATE);
        }
        return new WarehouseCandidate(warehouseId, coordinate,
                stocksByProductId == null ? Collections.emptyMap() : stocksByProductId);
    }

    public int availableQuantityOf(Long productId) {
        return stockOf(productId).availableQuantity();
    }

    public LocalDate earliestExpiryOf(Long productId) {
        return stockOf(productId).earliestExpiryDate();
    }

    public boolean hasAnyStock() {
        return stocksByProductId.values().stream()
                .anyMatch(stock -> stock.availableQuantity() > 0);
    }

    private ProductStock stockOf(Long productId) {
        return stocksByProductId.getOrDefault(productId, ProductStock.empty());
    }
}
