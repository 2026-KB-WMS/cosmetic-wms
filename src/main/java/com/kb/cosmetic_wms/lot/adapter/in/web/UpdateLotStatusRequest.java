package com.kb.cosmetic_wms.lot.adapter.in.web;

import com.kb.cosmetic_wms.lot.application.port.in.UpdateLotStatusCommand;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLotStatusRequest(

        @NotNull(message = "변경할 상태는 필수 입력 값입니다.")
        LotStatus status

) {
    public UpdateLotStatusCommand toCommand() {
        return new UpdateLotStatusCommand(status);
    }
}