package com.kb.cosmetic_wms.inbound.application.port.out;

import java.util.List;

public interface StorageQueryPort {

    boolean existsById(Long warehouseId);

    boolean canAccommodate(Long warehouseId, List<IncomingProduct> items);

    record IncomingProduct(Long productId, int quantity) {
    }
}