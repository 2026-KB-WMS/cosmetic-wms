package com.kb.cosmetic_wms.inbound.application.port.in;

public interface InboundLifecycleUseCase {

    InboundResult register(RegisterInboundCommand command);

    InboundResult receive(Long inboundId, ReceiveInboundCommand command);

    InboundResult cancel(Long inboundId);
}