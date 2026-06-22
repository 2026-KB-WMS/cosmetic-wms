package com.kb.cosmetic_wms.domain.inspection.dto;

import jakarta.validation.constraints.Min;

public record InspectionCompleteRequestDto(
        @Min(value = 0, message = "합격 수량은 0 이상이어야 합니다.")
        int passedQuantity,

        @Min(value = 0, message = "반려 수량은 0 이상이어야 합니다.")
        int failedQuantity,

        String defectReason
) {}
