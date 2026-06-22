package com.kb.cosmetic_wms.domain.inspection.dto;

import jakarta.validation.constraints.NotNull;

public record InspectionStartRequestDto(
        @NotNull(message = "검사자 ID는 필수입니다.")
        Long inspectorId
) {}
