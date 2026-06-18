package com.kb.cosmetic_wms.domain.lot.dto;

import com.kb.cosmetic_wms.domain.lot.enums.LotStatus;
import jakarta.validation.constraints.NotNull;

public record LotStatusUpdateRequestDto(

        @NotNull(message = "변경할 상태는 필수 입력 값입니다.")
        LotStatus status

) {
}
