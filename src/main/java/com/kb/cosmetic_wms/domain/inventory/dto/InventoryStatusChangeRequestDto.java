package com.kb.cosmetic_wms.domain.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryStatusChangeRequestDto(

        @Positive(message = "수량은 0보다 커야 합니다.")
        int quantity,

        Long referenceId,

        @NotNull(message = "작업자 ID는 필수입니다.")
        Long memberId

) {
}
