package com.kb.ordering.assignment.domain.model;

import com.kb.ordering.assignment.domain.exception.AssignmentErrorCode;
import com.kb.ordering.assignment.domain.exception.AssignmentValidationException;
import com.kb.ordering.global.geocoding.GeoCoordinate;
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
            throw new AssignmentValidationException(AssignmentErrorCode.INVALID_WAREHOUSE_CANDIDATE);
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
