package com.kb.cosmetic_wms.inbound.application.port.out;

public interface PartnerQueryPort {
    boolean existsById(Long partnerId);
}
