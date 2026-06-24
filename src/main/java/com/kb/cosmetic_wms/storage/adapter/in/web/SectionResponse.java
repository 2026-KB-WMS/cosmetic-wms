package com.kb.cosmetic_wms.storage.adapter.in.web;

import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.in.SectionResult;
import com.kb.cosmetic_wms.storage.domain.model.SectionAllocationStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionQualityStatus;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;

public record SectionResponse(
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
    public static SectionResponse from(SectionResult result) {
        return new SectionResponse(
                result.sectionId(),
                result.sectionCode(),
                result.sectionName(),
                result.sectionType(),
                result.qualityStatus(),
                result.allocationStatus(),
                result.temperatureType(),
                result.maxCapacity(),
                result.currentCapacity()
        );
    }
}