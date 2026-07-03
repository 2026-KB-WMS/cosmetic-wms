package com.kb.cosmetic_wms.oms.adapter.out.external;

import com.kb.cosmetic_wms.global.geocoding.GeoCoordinate;
import com.kb.cosmetic_wms.inventory.application.port.in.FindProductAvailabilityUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.ProductAvailabilitySlice;
import com.kb.cosmetic_wms.oms.application.port.out.LoadWarehouseCandidatesPort;
import com.kb.cosmetic_wms.oms.domain.model.ProductStock;
import com.kb.cosmetic_wms.oms.domain.model.WarehouseCandidate;
import com.kb.cosmetic_wms.storage.application.port.in.FindWarehouseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WarehouseCandidateAdapter implements LoadWarehouseCandidatesPort {

    private final FindWarehouseUseCase findWarehouseUseCase;
    private final FindProductAvailabilityUseCase findProductAvailabilityUseCase;

    @Override
    public List<WarehouseCandidate> loadCandidates(Collection<Long> productIds) {
        Map<Long, Map<Long, ProductStock>> stocksByWarehouseId =
                findProductAvailabilityUseCase.findAvailabilityByProducts(productIds).stream()
                        .collect(Collectors.groupingBy(
                                ProductAvailabilitySlice::warehouseId,
                                Collectors.toMap(
                                        ProductAvailabilitySlice::productId,
                                        slice -> new ProductStock(slice.availableQuantity(), slice.earliestExpiryDate())
                                )
                        ));

        return findWarehouseUseCase.findAll().stream()
                .map(warehouse -> WarehouseCandidate.of(
                        warehouse.warehouseId(),
                        new GeoCoordinate(warehouse.latitude(), warehouse.longitude()),
                        stocksByWarehouseId.getOrDefault(warehouse.warehouseId(), Map.of())
                ))
                .toList();
    }
}
