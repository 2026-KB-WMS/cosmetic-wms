package com.kb.cosmetic_wms.inbound.application.port.in;

import java.util.List;

public record ReceiveInboundCommand(List<LineItem> lines) {

    public record LineItem(Long lineId, int receivedQuantity) {}
}