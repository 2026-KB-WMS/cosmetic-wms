package com.kb.cosmetic_wms.domain.storage.dto;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.enums.SectionAllocationStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionQualityStatus;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;

public record SectionResponseDto(
        Long id,
        String sectionCode,
        String sectionName,
        SectionType sectionType,
        SectionQualityStatus qualityStatus,
        SectionAllocationStatus allocationStatus,
        TemperatureType temperatureType,
        int maxCapacity,
        int currentCapacity
) {
    public static SectionResponseDto from(Section section) {
        return new SectionResponseDto(
                section.getId(),
                section.getSectionCode(),
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
