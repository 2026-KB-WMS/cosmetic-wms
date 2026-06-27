package com.kb.cosmetic_wms.inbound.application.port.out;

import java.util.Collection;

public interface ProductQueryPort {
    boolean existsById(Long productId);

    boolean allExistByIds(Collection<Long> productIds);
}
