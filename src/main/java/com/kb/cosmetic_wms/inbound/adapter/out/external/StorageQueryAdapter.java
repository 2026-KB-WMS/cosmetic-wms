package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.storage.application.port.in.CheckWarehouseCapacityCommand;
import com.kb.cosmetic_wms.storage.application.port.in.CheckWarehouseCapacityUseCase;
import com.kb.cosmetic_wms.storage.application.port.in.FindWarehouseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StorageQueryAdapter implements StorageQueryPort {

    private final FindWarehouseUseCase findWarehouseUseCase;
    private final CheckWarehouseCapacityUseCase checkWarehouseCapacityUseCase;

    @Override
    public boolean existsById(Long warehouseId) {
        return findWarehouseUseCase.existsById(warehouseId);
    }

    @Override
    public boolean canAccommodate(Long warehouseId, List<IncomingProduct> items) {
        List<CheckWarehouseCapacityCommand.ProductQuantity> quantities = items.stream()
                .map(item -> new CheckWarehouseCapacityCommand.ProductQuantity(item.productId(), item.quantity()))
                .toList();
        return checkWarehouseCapacityUseCase.canAccommodate(
                new CheckWarehouseCapacityCommand(warehouseId, quantities));
    }
}
