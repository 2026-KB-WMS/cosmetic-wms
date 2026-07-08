package com.kb.cosmetic_wms.outbound.adapter.out.external;

import com.kb.cosmetic_wms.inventory.application.port.in.FindFefoInventoryUseCase;
import com.kb.cosmetic_wms.inventory.application.port.in.InventoryStatusChangeCommand;
import com.kb.cosmetic_wms.inventory.application.port.in.ManageInventoryStatusUseCase;
import com.kb.cosmetic_wms.outbound.application.port.out.FefoInventoryQueryPort;
import com.kb.cosmetic_wms.outbound.application.port.out.InventoryAllocationPort;
import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryAllocationAdapter implements FefoInventoryQueryPort, InventoryAllocationPort {

    private final FindFefoInventoryUseCase findFefoInventoryUseCase;
    private final ManageInventoryStatusUseCase manageInventoryStatusUseCase;

    @Override
    public List<AvailableStock> findAvailableForFefo(Long productId, Long warehouseId) {
        return findFefoInventoryUseCase.findAvailableForFefo(productId, warehouseId).stream()
                .map(slice -> new AvailableStock(slice.inventoryId(), slice.availableQuantity()))
                .toList();
    }

    @Override
    public void allocate(Long inventoryId, int quantity, Long outboundId, Long memberId) {
        manageInventoryStatusUseCase.allocate(inventoryId,
                new InventoryStatusChangeCommand(quantity, outboundId, memberId));
    }
}
