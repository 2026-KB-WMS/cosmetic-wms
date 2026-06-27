package com.kb.cosmetic_wms.inbound.application.port.out;

public interface ProductQueryPort {
    boolean existsById(Long productId);
}
