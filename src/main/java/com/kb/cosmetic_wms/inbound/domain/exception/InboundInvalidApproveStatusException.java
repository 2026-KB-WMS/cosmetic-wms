package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInvalidApproveStatusException extends BusinessException {

    public InboundInvalidApproveStatusException() {
        super(InboundErrorCode.INBOUND_INVALID_APPROVE_STATUS);
    }
}