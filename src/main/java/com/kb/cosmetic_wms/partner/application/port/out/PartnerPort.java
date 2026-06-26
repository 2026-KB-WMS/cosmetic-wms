package com.kb.cosmetic_wms.partner.application.port.out;

import com.kb.cosmetic_wms.partner.domain.model.Partner;

import java.util.Optional;

public interface PartnerPort {
    boolean existsByBusinessNumber(String businessNumber);
    Optional<Partner> findById(Long partnerId);
    Partner save(Partner partner);
}