package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidPutawayStatusException extends BusinessException {

    public InboundInvalidPutawayStatusException(String currentStatusDescription) {
        super(InboundErrorCode.INBOUND_INVALID_PUTAWAY_STATUS,
                String.format(InboundErrorCode.INBOUND_INVALID_PUTAWAY_STATUS.getMessage(), currentStatusDescription));
    }
}