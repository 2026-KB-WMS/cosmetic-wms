package com.kb.cosmetic_wms.outbound.application.port.in;

public interface OutboundLifecycleUseCase {

    OutboundResult createOutbound(CreateOutboundCommand command);

    OutboundResult allocateInventory(Long outboundId);

    OutboundResult startProcessing(Long outboundId);

    OutboundResult ship(Long outboundId);

    OutboundResult cancel(Long outboundId);
}