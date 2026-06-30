package com.kb.cosmetic_wms.putaway.application.port.in;

public interface CompletePutawayUseCase {
    PutawayOrderResult complete(Long putawayOrderId);
}
