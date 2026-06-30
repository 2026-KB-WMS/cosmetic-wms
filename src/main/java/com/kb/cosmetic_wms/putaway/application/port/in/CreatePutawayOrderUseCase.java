package com.kb.cosmetic_wms.putaway.application.port.in;

import java.util.List;

public interface CreatePutawayOrderUseCase {
    List<PutawayOrderResult> create(CreatePutawayOrderCommand command);
}
