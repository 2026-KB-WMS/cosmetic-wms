package com.kb.cosmetic_wms.oms.domain.model;

import com.kb.cosmetic_wms.oms.domain.exception.OmsErrorCode;
import com.kb.cosmetic_wms.oms.domain.exception.OmsValidationException;

public record DemandLine(Long productId, int quantity) {

    public DemandLine {
        if (productId == null || quantity <= 0) {
            throw new OmsValidationException(OmsErrorCode.INVALID_DEMAND);
        }
    }
}
