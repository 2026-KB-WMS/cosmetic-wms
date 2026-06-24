package com.kb.cosmetic_wms.partner.adapter.in.web;

import com.kb.cosmetic_wms.partner.application.port.in.PartnerResult;
import com.kb.cosmetic_wms.partner.domain.model.PartnerType;

public record PartnerResponse(
        Long id,
        String partnerName,
        PartnerType partnerType,
        String businessNumber
) {
    public static PartnerResponse from(PartnerResult result) {
        return new PartnerResponse(
                result.partnerId(),
                result.name(),
                result.type(),
                result.businessNumber()
        );
    }
}