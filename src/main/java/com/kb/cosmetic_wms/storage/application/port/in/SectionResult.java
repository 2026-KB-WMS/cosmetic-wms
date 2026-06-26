package com.kb.cosmetic_wms.storage.application.port.in;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.domain.model.Section;
import com.kb.cosmetic_wms.storage.domain.model.SectionAllocationStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionQualityStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;

public record SectionResult(
        Long sectionId,
        String sectionCode,
        String sectionName,
        SectionType sectionType,
        SectionQualityStatus qualityStatus,
        SectionAllocationStatus allocationStatus,
        TemperatureType temperatureType,
        int maxCapacity,
        int currentCapacity
) {
    public static SectionResult from(Section section) {
        return new SectionResult(
                section.getSectionId(),
                section.getSectionCode().value(),
                section.getSectionName(),
                section.getSectionType(),
                section.getQualityStatus(),
                section.getAllocationStatus(),
                section.getTemperatureType(),
                section.getMaxCapacity(),
                section.getCurrentCapacity()
        );
    }
}