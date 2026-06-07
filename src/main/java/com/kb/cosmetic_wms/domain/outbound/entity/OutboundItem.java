package com.kb.cosmetic_wms.domain.outbound.entity;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OutboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Outbound outbound;

    private Long orderItemId;
    private Long inventoryId;

    private int targetQuantity;
    private int pickedQuantity;

    private OutboundItem(Outbound outbound, Long orderItemId,
                         Long inventoryId, int targetQuantity) {
        this.outbound = outbound;
        this.orderItemId = orderItemId;
        this.inventoryId = inventoryId;
        this.targetQuantity = targetQuantity;
        this.pickedQuantity = 0;
    }

    public static OutboundItem create(Outbound outbound, Long orderItemId,
                                      Long inventoryId, int targetQuantity) {
        validateTargetQuantity(targetQuantity);

        return new OutboundItem(outbound, orderItemId, inventoryId, targetQuantity);
    }

    /**
     * 실제 피킹 완료 수량 변경
     *
     * @param pickedQuantity
     */
    public void changePickedQuantity(int pickedQuantity) {
        if (this.outbound.getOutboundStatus() != OutboundStatus.PICKING) {
            throw new IllegalStateException(OutboundConstants.INVALID_PICKING_STATUS_MESSAGE);
        }

        validatePickedQuantity(pickedQuantity);
        this.pickedQuantity = pickedQuantity;
    }

    public boolean isFullyPicked() {
        return this.targetQuantity == this.pickedQuantity;
    }

    private static void validateTargetQuantity(int targetQuantity) {
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException(OutboundConstants.INVALID_TARGET_QUANTITY_MESSAGE);
        }
    }

    private void validatePickedQuantity(int pickedQuantity) {
        if (pickedQuantity < 0) {
            throw new IllegalArgumentException(OutboundConstants.INVALID_PICKED_QUANTITY_MESSAGE);
        }
        if (pickedQuantity > this.targetQuantity) {
            throw new IllegalArgumentException(OutboundConstants.EXCEED_PICKED_QUANTITY_MESSAGE);
        }
    }
}
