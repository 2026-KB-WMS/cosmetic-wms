package com.kb.cosmetic_wms.partner.application.port.in;

public interface FindPartnerUseCase {
    PartnerResult findById(Long partnerId);
}