package com.kb.cosmetic_wms.inbound.application.port.in;

public interface InboundLifecycleUseCase {

    InboundResult register(RegisterInboundCommand command);

    InboundResult start(Long inboundId);

    InboundResult complete(Long inboundId);

    InboundResult cancel(Long inboundId);
}
