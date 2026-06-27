package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundInspectionIncompleteException extends BusinessException {

    public InboundInspectionIncompleteException() {
        super(InboundErrorCode.INBOUND_INSPECTION_INCOMPLETE);
    }
}