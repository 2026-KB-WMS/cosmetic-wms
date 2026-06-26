package com.kb.cosmetic_wms.outbound.application.port.in;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;

import java.util.List;

public record CreateOutboundCommand(
        Long ordersId,
        Long warehouseId,
        OutboundType outboundType,
        List<OutboundLine> lines
) {
}