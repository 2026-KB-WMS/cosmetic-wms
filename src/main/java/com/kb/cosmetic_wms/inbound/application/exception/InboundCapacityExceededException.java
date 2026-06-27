package com.kb.cosmetic_wms.inbound.application.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundErrorCode;

public class InboundCapacityExceededException extends BusinessException {

    public InboundCapacityExceededException() {
        super(InboundErrorCode.INBOUND_WAREHOUSE_CAPACITY_EXCEEDED);
    }
}