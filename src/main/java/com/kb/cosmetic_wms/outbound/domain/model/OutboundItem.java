package com.kb.cosmetic_wms.outbound.domain.model;

import lombok.Getter;

@Getter
public class OutboundItem {

    private Long id;
    private Long orderItemId;
    private Long inventoryId;
    private PickingQuantity pickingQuantity;

    OutboundItem(OutboundLine line) {
        this.orderItemId = line.orderItemId();
        this.inventoryId = line.inventoryId();
        this.pickingQuantity = PickingQuantity.initial(line.targetQuantity());
    }

    private OutboundItem() {
    }

    public static OutboundItem reconstitute(Long id, Long orderItemId, Long inventoryId,
                                            int targetQuantity, int pickedQuantity) {
        OutboundItem item = new OutboundItem();
        item.id = id;
        item.orderItemId = orderItemId;
        item.inventoryId = inventoryId;
        item.pickingQuantity = new PickingQuantity(targetQuantity, pickedQuantity);
        return item;
    }

    void changePickedQuantity(int pickedQuantity) {
        this.pickingQuantity = this.pickingQuantity.pick(pickedQuantity);
    }

    public boolean isFullyPicked() {
        return this.pickingQuantity.isFullyPicked();
    }

    public int getTargetQuantity() {
        return this.pickingQuantity.target();
    }

    public int getPickedQuantity() {
        return this.pickingQuantity.picked();
    }
}