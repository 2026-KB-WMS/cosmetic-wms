package com.kb.cosmetic_wms.inventory.application.port.in;

public interface CreateInventoryFromInboundUseCase {
    void createFromInbound(InboundPutawayCommand command);
}
