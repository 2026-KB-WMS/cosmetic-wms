package com.kb.cosmetic_wms.inventory.application.port.in;

import java.util.Collection;
import java.util.List;

public interface FindProductAvailabilityUseCase {

    List<ProductAvailabilitySlice> findAvailabilityByProducts(Collection<Long> productIds);
}
