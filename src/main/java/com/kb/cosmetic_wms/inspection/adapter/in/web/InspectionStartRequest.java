package com.kb.cosmetic_wms.inspection.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record InspectionStartRequest(
        @NotNull(message = "검사자 ID는 필수입니다.")
        Long inspectorId
) {}