package com.kb.cosmetic_wms.inbound.application.port.in;

public interface InboundItemUseCase {

    InboundResult addItem(Long inboundId, AddInboundItemCommand command);

    InboundItemResult completePutaway(Long inboundId, Long itemId, PutawayCommand command);

    InboundItemResult approve(Long inboundId, Long itemId);

    InboundItemResult hold(Long inboundId, Long itemId);
}
