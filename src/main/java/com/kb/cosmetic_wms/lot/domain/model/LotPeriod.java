package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.exception.InvalidManufactureDateException;
import com.kb.cosmetic_wms.lot.domain.exception.LotDatesRequiredException;

import java.time.LocalDateTime;

public record LotPeriod(LocalDateTime manufacturingDate, LocalDateTime expirationDate) {

    public LotPeriod {
        if (manufacturingDate == null || expirationDate == null) {
            throw new LotDatesRequiredException();
        }
        if (manufacturingDate.isAfter(expirationDate)) {
            throw new InvalidManufactureDateException();
        }
    }
}