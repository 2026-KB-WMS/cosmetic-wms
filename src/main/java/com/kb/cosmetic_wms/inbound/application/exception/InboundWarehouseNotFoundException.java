package com.kb.cosmetic_wms.inbound.application.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundErrorCode;

public class InboundWarehouseNotFoundException extends BusinessException {

    public InboundWarehouseNotFoundException() {
        super(InboundErrorCode.INBOUND_WAREHOUSE_NOT_FOUND);
    }
}