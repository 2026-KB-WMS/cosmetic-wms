package com.kb.cosmetic_wms.inventory.application.port.in;

public interface ReleaseInventoryForOutboundUseCase {
    void releaseForOutbound(Long outboundId, Long memberId);
}