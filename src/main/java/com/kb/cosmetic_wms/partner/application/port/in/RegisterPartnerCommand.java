package com.kb.cosmetic_wms.partner.application.port.in;

import com.kb.cosmetic_wms.partner.domain.model.PartnerType;

public record RegisterPartnerCommand(
        String name,
        PartnerType type,
        String businessNumber
) {
}