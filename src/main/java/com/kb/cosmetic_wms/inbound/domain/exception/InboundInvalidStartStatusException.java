package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidStartStatusException extends BusinessException {

    public InboundInvalidStartStatusException(String currentStatusDescription) {
        super(InboundErrorCode.INBOUND_INVALID_START_STATUS,
                String.format(InboundErrorCode.INBOUND_INVALID_START_STATUS.getMessage(), currentStatusDescription));
    }
}