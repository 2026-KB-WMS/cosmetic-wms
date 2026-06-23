package com.kb.cosmetic_wms.storage.adapter.in.web;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.storage.application.port.in.AddSectionCommand;
import com.kb.cosmetic_wms.storage.domain.model.SectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionCreateRequest(
        @NotNull(message = "섹션 타입은 필수 입력 항목입니다.")
        SectionType sectionType,

        @NotBlank(message = "섹션 이름은 필수 입력 항목입니다.")
        String sectionName,

        @NotNull(message = "온도 타입은 필수 입력 항목입니다.")
        TemperatureType temperatureType,

        @Min(value = 1, message = "최대 수용 용량은 최소 1 이상이어야 합니다.")
        int maxCapacity
) {
    public AddSectionCommand toCommand() {
        return new AddSectionCommand(sectionType, sectionName, temperatureType, maxCapacity);
    }
}