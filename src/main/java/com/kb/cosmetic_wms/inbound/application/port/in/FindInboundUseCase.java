package com.kb.cosmetic_wms.inbound.application.port.in;

public interface FindInboundUseCase {

    InboundResult findById(Long inboundId);
}
