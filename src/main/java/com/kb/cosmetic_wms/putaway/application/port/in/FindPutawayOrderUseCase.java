package com.kb.cosmetic_wms.putaway.application.port.in;

public interface FindPutawayOrderUseCase {
    PutawayOrderResult findById(Long putawayOrderId);
}
