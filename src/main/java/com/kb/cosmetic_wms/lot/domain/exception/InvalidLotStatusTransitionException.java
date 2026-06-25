package com.kb.cosmetic_wms.lot.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;
import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;

public class InvalidLotStatusTransitionException extends BusinessException {

    public InvalidLotStatusTransitionException(LotStatus from, LotStatus to) {
        super(LotErrorCode.INVALID_LOT_STATUS_TRANSITION,
                from.name() + " 상태에서 " + to.name() + " 상태로 변경할 수 없습니다.");
    }
}