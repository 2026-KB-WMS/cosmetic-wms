package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.exception.InboundExpirationBeforeManufactureException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundExpirationDateRequiredException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundManufactureDateRequiredException;

import java.time.LocalDate;

public record LotDateRange(LocalDate manufactureDate, LocalDate expirationDate) {

    public LotDateRange {
        if (manufactureDate == null) {
            throw new InboundManufactureDateRequiredException();
        }
        if (expirationDate == null) {
            throw new InboundExpirationDateRequiredException();
        }
        if (!expirationDate.isAfter(manufactureDate)) {
            throw new InboundExpirationBeforeManufactureException();
        }
    }
}