package com.kb.cosmetic_wms.inbound.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InboundManufactureDateRequiredException extends BusinessException {

    public InboundManufactureDateRequiredException() {
        super(InboundErrorCode.INBOUND_MANUFACTURE_DATE_REQUIRED);
    }
}