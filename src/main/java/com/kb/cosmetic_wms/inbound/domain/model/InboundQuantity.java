package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.exception.InboundInvalidQuantityException;

public record InboundQuantity(int value) {

    public InboundQuantity {
        if (value <= 0) {
            throw new InboundInvalidQuantityException();
        }
    }
}