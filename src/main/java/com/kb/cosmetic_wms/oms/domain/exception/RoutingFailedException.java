package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class RoutingFailedException extends BusinessException {

    public RoutingFailedException() {
        super(OmsErrorCode.ROUTING_API_ERROR);
    }
}
