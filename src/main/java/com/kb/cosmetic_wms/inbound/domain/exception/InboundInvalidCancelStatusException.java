package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidCancelStatusException extends BusinessException {

    public InboundInvalidCancelStatusException(String currentStatusDescription) {
        super(InboundErrorCode.INBOUND_INVALID_CANCEL_STATUS,
                String.format(InboundErrorCode.INBOUND_INVALID_CANCEL_STATUS.getMessage(), currentStatusDescription));
    }
}