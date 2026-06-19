package com.kb.cosmetic_wms.domain.inbound.dto;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import jakarta.validation.constraints.NotNull;

public record InboundPutawayRequestDto(
        @NotNull(message = InboundConstants.PUTAWAY_LOT_REQUIRED_MESSAGE)
        Long lotId,

        @NotNull(message = InboundConstants.PUTAWAY_SECTION_REQUIRED_MESSAGE)
        Long sectionId
) {
}
