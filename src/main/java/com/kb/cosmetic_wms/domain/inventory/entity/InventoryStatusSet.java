package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public record InventoryStatusSet(
        @Enumerated(EnumType.STRING)
        AllocStatus allocStatus,

        @Enumerated(EnumType.STRING)
        QualityStatus qualityStatus,

        @Enumerated(EnumType.STRING)
        LocStatus locStatus
) {
    public static InventoryStatusSet of(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        validateStatusCombination(allocStatus, qualityStatus, locStatus);
        return new InventoryStatusSet(allocStatus, qualityStatus, locStatus);
    }

    private static void validateStatusCombination(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        if (allocStatus == null || qualityStatus == null || locStatus == null) {
            throw new IllegalArgumentException("재고의 모든 상태값은 필수입니다.");
        }

        if (allocStatus != AllocStatus.UNALLOCATED && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException("할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다.");
        }

        if (locStatus == LocStatus.MOVING && allocStatus != AllocStatus.UNALLOCATED) {
            throw new IllegalArgumentException("이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.");
        }

        if (locStatus == LocStatus.MOVING && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException(
                    String.format("품질 상태가 %s인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.", qualityStatus.getDescription())
            );
        }
    }
}
