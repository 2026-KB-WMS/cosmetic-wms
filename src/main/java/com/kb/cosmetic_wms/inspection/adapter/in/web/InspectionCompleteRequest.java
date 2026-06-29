package com.kb.cosmetic_wms.inspection.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InspectionCompleteRequest(
        @Min(value = 0, message = "합격 수량은 0 이상이어야 합니다.")
        int passedQuantity,

        @Min(value = 0, message = "반려 수량은 0 이상이어야 합니다.")
        int failedQuantity,

        String defectReason,

        @NotNull(message = "배치 구역 ID는 필수입니다.")
        Long sectionId
) {}
