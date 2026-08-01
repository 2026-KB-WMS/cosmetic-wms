package com.kb.ordering.assignment.adapter.out.external;

import com.kb.ordering.assignment.application.port.out.FindProductAvailabilityPort;
import com.kb.ordering.assignment.application.port.out.FindWarehousePort;
import com.kb.ordering.assignment.application.port.out.LoadWarehouseCandidatesPort;
import com.kb.ordering.assignment.application.port.out.dto.ProductAvailabilityView;
import com.kb.ordering.assignment.domain.model.ProductStock;
import com.kb.ordering.assignment.domain.model.WarehouseCandidate;
import com.kb.ordering.global.geocoding.GeoCoordinate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @Component
@RequiredArgsConstructor
public class WarehouseCandidateAdapter implements LoadWarehouseCandidatesPort {

    private final FindWarehousePort findWarehousePort;
    private final FindProductAvailabilityPort findProductAvailabilityPort;

    @Override
    public List<WarehouseCandidate> loadCandidates(Collection<Long> productIds) {
        Map<Long, Map<Long, ProductStock>> stocksByWarehouseId =
                findProductAvailabilityPort.findAvailabilityByProducts(productIds).stream()
                        .collect(Collectors.groupingBy(
                                ProductAvailabilityView::warehouseId,
                                Collectors.toMap(
                                        ProductAvailabilityView::productId,
                                        view -> new ProductStock(view.availableQuantity(), view.earliestExpiryDate())
                                )
                        ));

        return findWarehousePort.findAll().stream()
                .map(warehouse -> WarehouseCandidate.of(
                        warehouse.warehouseId(),
                        new GeoCoordinate(warehouse.latitude(), warehouse.longitude()),
                        stocksByWarehouseId.getOrDefault(warehouse.warehouseId(), Map.of())
                ))
                .toList();
    }
}
