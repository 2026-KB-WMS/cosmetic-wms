package com.kb.cosmetic_wms.domain.partner.dto;

import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;

public record PartnerResponseDto(
        Long id,
        String partnerName,
        PartnerType partnerType,
        String businessNumber
) {
    public static PartnerResponseDto from(Partner partner) {
        return new PartnerResponseDto(
                partner.getId(),
                partner.getName(),
                partner.getType(),
                partner.getBusinessNumber()
        );
    }
}
