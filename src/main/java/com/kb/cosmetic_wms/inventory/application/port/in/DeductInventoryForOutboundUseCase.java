package com.kb.cosmetic_wms.inventory.application.port.in;

public interface DeductInventoryForOutboundUseCase {
    void deductForOutbound(Long outboundId, Long memberId);
}
