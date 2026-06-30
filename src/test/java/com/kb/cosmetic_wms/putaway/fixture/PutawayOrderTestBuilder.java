package com.kb.cosmetic_wms.putaway.fixture;

import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;

public class PutawayOrderTestBuilder {

    private Long inspectionId = 1L;
    private Long lotId = 10L;
    private Long productId = 100L;
    private Long warehouseId = 1L;
    private Long sourceSectionId = 5L;
    private Long targetSectionId = 10L;
    private int quantity = 50;
    private boolean normalQuality = true;

    public PutawayOrderTestBuilder inspectionId(Long inspectionId) {
        this.inspectionId = inspectionId;
        return this;
    }

    public PutawayOrderTestBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public PutawayOrderTestBuilder targetSectionId(Long targetSectionId) {
        this.targetSectionId = targetSectionId;
        return this;
    }

    public PutawayOrderTestBuilder normalQuality(boolean normalQuality) {
        this.normalQuality = normalQuality;
        return this;
    }

    public PutawayOrder build() {
        return PutawayOrder.create(inspectionId, lotId, productId, warehouseId,
                sourceSectionId, targetSectionId, quantity, normalQuality);
    }

    public PutawayOrder buildCompleted() {
        PutawayOrder order = build();
        order.complete();
        return order;
    }
}
