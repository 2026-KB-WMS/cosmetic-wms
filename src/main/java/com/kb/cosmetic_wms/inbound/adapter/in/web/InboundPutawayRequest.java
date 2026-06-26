package com.kb.cosmetic_wms.inbound.adapter.in.web;

import com.kb.cosmetic_wms.inbound.domain.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;

public record InboundPutawayRequest(
        @NotNull(message = InboundConstants.PUTAWAY_LOT_REQUIRED_MESSAGE) Long lotId,
        @NotNull(message = InboundConstants.PUTAWAY_SECTION_REQUIRED_MESSAGE) Long sectionId
) {
}
