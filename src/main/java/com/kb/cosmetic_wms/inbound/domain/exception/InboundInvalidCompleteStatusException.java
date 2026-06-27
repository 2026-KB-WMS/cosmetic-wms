package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidCompleteStatusException extends BusinessException {

    public InboundInvalidCompleteStatusException(String currentStatusDescription) {
        super(InboundErrorCode.INBOUND_INVALID_COMPLETE_STATUS,
                String.format(InboundErrorCode.INBOUND_INVALID_COMPLETE_STATUS.getMessage(), currentStatusDescription));
    }
}