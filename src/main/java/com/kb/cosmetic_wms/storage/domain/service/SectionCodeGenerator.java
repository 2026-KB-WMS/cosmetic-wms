package com.kb.cosmetic_wms.storage.domain.service;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.StorageValidationException;
import com.kb.cosmetic_wms.storage.domain.model.SectionCode;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;

public class SectionCodeGenerator {

    public SectionCode generate(String warehouseCode, SectionType sectionType,
                                TemperatureType temperatureType, int sequence) {
        if (sequence <= 0) {
            throw new StorageValidationException(StorageErrorCode.INVALID_SECTION_SEQUENCE);
        }
        String code = String.format("%s-%s-%s-%02d",
                warehouseCode,
                sectionType.getShortCode(),
                temperatureType.getShortCode(),
                sequence);
        return new SectionCode(code);
    }
}