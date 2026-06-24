package com.kb.cosmetic_wms.partner.application.port.in;

import com.kb.cosmetic_wms.partner.domain.model.Partner;
import com.kb.cosmetic_wms.partner.domain.model.PartnerType;

public record PartnerResult(
        Long partnerId,
        String name,
        PartnerType type,
        String businessNumber
) {
    public static PartnerResult from(Partner partner) {
        return new PartnerResult(
                partner.getPartnerId(),
                partner.getName(),
                partner.getType(),
                partner.getBusinessNumber()
        );
    }
}