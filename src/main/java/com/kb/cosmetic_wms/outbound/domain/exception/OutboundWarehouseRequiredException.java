package com.kb.cosmetic_wms.outbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class OutboundWarehouseRequiredException extends BusinessException {
    public OutboundWarehouseRequiredException() {
        super(OutboundErrorCode.OUTBOUND_WAREHOUSE_REQUIRED);
    }
}