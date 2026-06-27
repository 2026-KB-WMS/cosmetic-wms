package com.kb.cosmetic_wms.storage.application.port.in;

import com.kb.cosmetic_wms.storage.domain.model.TemperatureZone;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;

public record AddSectionCommand(
        SectionType sectionType,
        String sectionName,
        TemperatureZone temperatureType,
        int maxCapacity
) {
}