package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundWarehouseIdRequiredException extends BusinessException {

    public InboundWarehouseIdRequiredException() {
        super(InboundErrorCode.INBOUND_WAREHOUSE_ID_REQUIRED);
    }
}