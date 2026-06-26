package com.kb.cosmetic_wms.partner.domain.model;

import com.kb.cosmetic_wms.partner.domain.exception.PartnerValidationException;
import lombok.Getter;

@Getter
public class Partner {

    private final Long partnerId;
    private final String name;
    private final PartnerType type;
    private final String businessNumber;

    private Partner(Long partnerId, String name, PartnerType type, String businessNumber) {
        this.partnerId = partnerId;
        this.name = name;
        this.type = type;
        this.businessNumber = businessNumber;
    }

    public static Partner create(String name, PartnerType type, String businessNumber) {
        validateName(name);
        validateType(type);
        return new Partner(null, name, type, businessNumber);
    }

    public static Partner reconstitute(Long partnerId, String name, PartnerType type, String businessNumber) {
        return new Partner(partnerId, name, type, businessNumber);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new PartnerValidationException();
        }
    }

    private static void validateType(PartnerType type) {
        if (type == null) {
            throw new PartnerValidationException();
        }
    }
}