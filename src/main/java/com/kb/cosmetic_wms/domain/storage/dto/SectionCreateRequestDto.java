package com.kb.cosmetic_wms.domain.storage.dto;

import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.storage.enums.SectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SectionCreateRequestDto(
        @NotNull(message = "섹션 타입은 필수 입력 항목입니다.")
        SectionType sectionType,

        @NotBlank(message = "섹션 코드는 필수 입력 항목입니다.")
        @Pattern(
                regexp = "^WH\\d{2}-(DOCK|HIGH|MID|LOW|QUAR)-[RC]-\\d{2}$",
                message = "섹션 코드 포맷이 올바르지 않습니다. (예: WH01-HIGH-R-01)"
        )
        String sectionCode,

        @NotBlank(message = "섹션 이름은 필수 입력 항목입니다.")
        String sectionName,

        @NotNull(message = "온도 타입은 필수 입력 항목입니다.")
        TemperatureType temperatureType,

        @Min(value = 1, message = "최대 수용 용량은 최소 1 이상이어야 합니다.")
        int maxCapacity
) {
}
