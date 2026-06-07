package com.kb.cosmetic_wms.domain.outbound.entity;

import com.kb.cosmetic_wms.domain.outbound.OutboundLine;
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

    OutboundItem(Outbound outbound, OutboundLine line) {
        validateTargetQuantity(line.targetQuantity());

        this.outbound = outbound;
        this.orderItemId = line.orderItemId();
        this.inventoryId = line.inventoryId();
        this.targetQuantity = line.targetQuantity();
        this.pickedQuantity = 0;
    }

    /**
     * 물류 현장의 실제 피킹 완료 수량을 반영한다.
     *
     * @param pickedQuantity 현장에서 실제 피킹 완료한 수량
     * @throws IllegalStateException    상위 출고 전표의 라이프사이클 상태가 PICKING이 아닌 경우
     * @throws IllegalArgumentException 피킹 수량이 음수이거나 지시 수량을 초과한 경우
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
