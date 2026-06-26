package com.kb.cosmetic_wms.outbound.domain.model;

import com.kb.cosmetic_wms.outbound.domain.exception.OutboundExceedPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidPickedQuantityException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInvalidTargetQuantityException;

public record PickingQuantity(int target, int picked) {

    public PickingQuantity {
        if (target <= 0) throw new OutboundInvalidTargetQuantityException();
        if (picked < 0) throw new OutboundInvalidPickedQuantityException();
        if (picked > target) throw new OutboundExceedPickedQuantityException();
    }

    public static PickingQuantity initial(int target) {
        return new PickingQuantity(target, 0);
    }

    public PickingQuantity pick(int newPicked) {
        return new PickingQuantity(this.target, newPicked);
    }

    public boolean isFullyPicked() {
        return this.target == this.picked;
    }
}