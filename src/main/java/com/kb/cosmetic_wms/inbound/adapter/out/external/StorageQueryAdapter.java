package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.storage.application.port.out.StoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageQueryAdapter implements StorageQueryPort {

    private final StoragePort storagePort;

    @Override
    public boolean existsById(Long warehouseId) {
        return storagePort.existsById(warehouseId);
    }
}
