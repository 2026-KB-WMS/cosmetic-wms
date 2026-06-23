package com.kb.cosmetic_wms.storage.application.port.in;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;

public record AddSectionCommand(
        SectionType sectionType,
        String sectionName,
        TemperatureType temperatureType,
        int maxCapacity
) {
}