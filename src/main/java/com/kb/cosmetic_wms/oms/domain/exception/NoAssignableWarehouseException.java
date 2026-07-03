package com.kb.cosmetic_wms.oms.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class NoAssignableWarehouseException extends BusinessException {

    public NoAssignableWarehouseException() {
        super(OmsErrorCode.NO_ASSIGNABLE_WAREHOUSE);
    }
}
