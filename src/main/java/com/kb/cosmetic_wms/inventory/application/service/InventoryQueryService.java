package com.kb.cosmetic_wms.inventory.application.service;

import com.kb.cosmetic_wms.inventory.application.port.in.FefoInventorySlice;
import com.kb.cosmetic_wms.inventory.application.port.in.FindFefoInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.FindInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.FindProductAvailabilityUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.InventoryResult;
import com.kb.cosmetic_wms.inventory.application.port.in.ProductAvailabilitySlice;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryQueryService implements
        FindInventoryUseCase,
        FindFefoInventoryUseCase,
        FindProductAvailabilityUseCase {

    private final InventoryPort inventoryPort;

    @Override
    public InventoryResult findById(Long inventoryId) {
        Inventory inventory = inventoryPort.findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);
        return InventoryResult.from(inventory);
    }

    @Override
    public List<InventoryResult> findByLotId(Long lotId) {
        return inventoryPort.findByLotId(lotId).stream()
                .map(InventoryResult::from)
                .toList();
    }

    @Override
    public List<InventoryResult> findByProductId(Long productId) {
        return inventoryPort.findByProductId(productId).stream()
                .map(InventoryResult::from)
                .toList();
    }

    @Override
    public List<FefoInventorySlice> findAvailableForFefo(Long productId, Long warehouseId) {
        return inventoryPort.findAvailableForFefo(productId, warehouseId).stream()
                .map(view -> new FefoInventorySlice(view.inventoryId(), view.availableQuantity()))
                .toList();
    }

    @Override
    public List<ProductAvailabilitySlice> findAvailabilityByProducts(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return inventoryPort.findAvailabilityByProducts(productIds).stream()
                .map(view -> new ProductAvailabilitySlice(
                        view.warehouseId(), view.productId(),
                        view.availableQuantity(), view.earliestExpiryDate()))
                .toList();
    }
}
