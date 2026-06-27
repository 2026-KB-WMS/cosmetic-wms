package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidReceiveStatusException extends BusinessException {

    public InboundInvalidReceiveStatusException(String currentStatusDescription) {
        super(InboundErrorCode.INBOUND_INVALID_RECEIVE_STATUS,
                String.format(InboundErrorCode.INBOUND_INVALID_RECEIVE_STATUS.getMessage(), currentStatusDescription));
    }
}
