package com.kb.cosmetic_wms.putaway.application.port.out;

import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;

import java.util.Optional;

public interface PutawayOrderPort {
    PutawayOrder save(PutawayOrder putawayOrder);
    Optional<PutawayOrder> findById(Long putawayOrderId);
    Optional<PutawayOrder> findByIdForUpdate(Long putawayOrderId);
}
