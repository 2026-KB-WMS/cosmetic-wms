package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private Long lotId;
    private Long sectionId;
    private Long warehouseId;

    private int quantity;

    private int availableQuantity;

    @Embedded
    private InventoryStatusSet statusSet;

    private Inventory(Long productId, Long lotId, Long sectionId, Long warehouseId,
                      int quantity, int availableQuantity, InventoryStatusSet statusSet) {
        this.productId = productId;
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.warehouseId = warehouseId;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.statusSet = statusSet;
    }

    public static Inventory create(Long productId, Long lotId, Long sectionId, Long warehouseId,
                                   int quantity, int availableQuantity, InventoryStatusSet statusSet) {
        validateQuantity(quantity);
        validateAvailableQuantity(quantity, availableQuantity);
        validateAvailableQuantityForQualityStatus(statusSet, availableQuantity);

        return new Inventory(productId, lotId, sectionId, warehouseId,
                quantity, availableQuantity, statusSet);
    }

    /**
     * 출고 할당 처리 (UNALLOCATED -> ALLOCATED 재고 분할)
     * <p>주문 배정을 위해 가용 수량 내에서 요청 수량만큼 출고 상태로 제외시킵니다.</p>
     *
     * @param allocQuantity 할당(배정) 요청 수량
     * @return 할당 상태로 분할되어 떨어져 나간 새로운 Inventory 객체 (전체 할당 시 자기 자신)
     * @throws IllegalArgumentException 할당 요청 수량이 가용 수량을 초과하거나 0 이하인 경우
     */
    public Inventory allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_ALLOCATE_QUANTITY_MESSAGE);
        }
        if (this.availableQuantity < allocQuantity) {
            throw new IllegalArgumentException(InventoryConstants.LACK_OF_AVAILABLE_QUANTITY_MESSAGE);
        }

        InventoryStatusSet allocatedStatusSet = InventoryStatusSet.of(
                AllocStatus.ALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );

        // 전체 수량을 통째로 할당하는 경우
        if (this.quantity == allocQuantity) {
            this.availableQuantity = 0;
            this.statusSet = allocatedStatusSet;
            return this;
        }

        // 수량이 분할되는 경우
        this.quantity -= allocQuantity;
        this.availableQuantity -= allocQuantity;

        return new Inventory(
                this.productId, this.lotId, this.sectionId, this.warehouseId,
                allocQuantity, 0, allocatedStatusSet
        );
    }

    /**
     * 출고 할당 취소 (ALLOCATED -> UNALLOCATED 복귀)
     * <p>배정되었던 주문이 취소되거나 변경되었을 때, 할당 재고를 다시 일반 미할당(가용) 재고로 되돌립니다.</p>
     *
     * @param targetQuantity 할당 취소 요청 수량
     * @return 미할당 상태로 복귀 및 분할된 Inventory 객체
     * @throws IllegalStateException 할당(ALLOCATED) 상태의 재고가 아닐 경우
     */
    public Inventory unallocate(int targetQuantity) {
        if (this.statusSet.allocStatus() != AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.UNALLOCATE_FOR_ALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                AllocStatus.UNALLOCATED, this.statusSet.qualityStatus(), this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 창고 내 재고 이동 시작 (STORED -> MOVING)
     * <p>로케이션 이동(이적) 작업을 위해 특정 수량만큼 이동 중 상태로 변경합니다.</p>
     *
     * @param targetQuantity 이동 대상 수량
     * @return 이동 중(MOVING) 상태로 분할된 Inventory 객체
     * @throws IllegalStateException 이미 할당되었거나 이동 중인 재고인 경우
     */
    public Inventory startMoving(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_MOVING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        if (this.statusSet.locStatus() == LocStatus.MOVING) {
            throw new IllegalStateException(InventoryConstants.ALREADY_MOVING_INVENTORY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.MOVING
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 창고 내 재고 이동 완료 (MOVING -> STORED)
     * <p>이동 중이던 실물 재고가 목적지 랙/섹션에 물리적 안착이 끝났을 때 보관중 상태로 복귀시킵니다.</p>
     *
     * @param targetQuantity 이동 완료 처리할 수량
     * @return 보관 완료(STORED) 상태로 복귀 및 분할된 Inventory 객체
     * @throws IllegalStateException 이동 중(MOVING) 상태의 재고가 아닐 경우
     */
    public Inventory finishMoving(int targetQuantity) {
        if (this.statusSet.locStatus() != LocStatus.MOVING) {
            throw new IllegalStateException(InventoryConstants.FINISH_MOVING_FOR_MOVING_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), this.statusSet.qualityStatus(), LocStatus.STORED
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 검수 시작 (정상 -> INSPECTING)
     * <p>반품 입고나 수시 검사 사유 발생 시, 특정 재고를 품질 검수 대상 격리 상태로 변경합니다.</p>
     *
     * @param targetQuantity 검수 대상 수량
     * @return 검수 중(INSPECTING) 상태로 분할된 Inventory 객체
     * @throws IllegalStateException 이미 출고 할당된 재고인 경우
     */
    public Inventory startInspecting(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.START_INSPECTING_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.INSPECTING, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 검수 통과 / 보류 해제 (불량/검수중 -> NORMAL)
     * <p>품질 판정이 완료되어 정상품 진단을 받은 재고를 출고 가능하도록 정상 재고 상태로 복귀시킵니다.</p>
     *
     * @param targetQuantity 정상 복귀 대상 수량
     * @return 정상(NORMAL) 상태로 복귀 및 분할된 Inventory 객체
     * @throws IllegalStateException 이미 출고 할당된 재고인 경우
     */
    public Inventory restoreToNormalQuality(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.CHANGE_QUALITY_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.NORMAL, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 품질 이슈로 인한 출고 금지 처리 (정상 -> HOLD)
     * <p>현장 실물 훼손 징후나 성분 이슈 보고 시, 출고 가용 수량에서 즉시 격리하기 위해 보류 상태로 잠금 처리합니다.</p>
     *
     * @param targetQuantity 보류(잠금) 대상 수량
     * @return 보류(HOLD) 상태로 변경 및 분할된 Inventory 객체
     * @throws IllegalStateException 이미 출고 할당된 재고인 경우
     */
    public Inventory holdForQualityIssue(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.HOLD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.HOLD, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    /**
     * 폐기 예정 처리 (-> DISCARD_SCHEDULED)
     * <p>유통기한 지남, 파손 등으로 폐기 처분 재고 상태로 변경합니다.</p>
     *
     * @param targetQuantity 폐기 예정 대상 수량
     * @return 폐기 예정(DISCARD_SCHEDULED) 상태로 변경 및 분할된 Inventory 객체
     * @throws IllegalStateException 이미 출고 할당된 재고인 경우
     */
    public Inventory scheduleForDiscard(int targetQuantity) {
        if (this.statusSet.allocStatus() == AllocStatus.ALLOCATED) {
            throw new IllegalStateException(InventoryConstants.DISCARD_FOR_UNALLOCATED_ONLY_MESSAGE);
        }

        InventoryStatusSet nextStatusSet = InventoryStatusSet.of(
                this.statusSet.allocStatus(), QualityStatus.DISCARD_SCHEDULED, this.statusSet.locStatus()
        );
        return splitAndChangeStatus(targetQuantity, nextStatusSet);
    }

    private Inventory splitAndChangeStatus(int targetQuantity, InventoryStatusSet nextStatusSet) {
        if (targetQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_QUANTITY_MESSAGE);
        }
        if (this.quantity < targetQuantity) {
            throw new IllegalArgumentException(InventoryConstants.EXCEED_INVENTORY_QUANTITY_MESSAGE);
        }

        int nextAvailableQuantity = 0;
        if (nextStatusSet.qualityStatus().isNormal() && nextStatusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            nextAvailableQuantity = targetQuantity;
        }

        if (this.quantity == targetQuantity) {
            this.statusSet = nextStatusSet;
            this.availableQuantity = nextAvailableQuantity;
            return this;
        }

        this.quantity -= targetQuantity;

        if (this.statusSet.qualityStatus().isNormal() && this.statusSet.allocStatus() == AllocStatus.UNALLOCATED) {
            this.availableQuantity -= targetQuantity;
        }

        return new Inventory(
                this.productId, this.lotId, this.sectionId, this.warehouseId,
                targetQuantity, nextAvailableQuantity, nextStatusSet
        );
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_QUANTITY_MESSAGE);
        }
    }

    private static void validateAvailableQuantity(int quantity, int availableQuantity) {
        if (quantity < availableQuantity) {
            throw new IllegalArgumentException(InventoryConstants.OVER_AVAILABLE_QUANTITY_MESSAGE);
        }
    }

    private static void validateAvailableQuantityForQualityStatus(InventoryStatusSet statusSet, int availableQuantity) {
        if (!statusSet.qualityStatus().isNormal() && availableQuantity > 0) {
            throw new IllegalArgumentException(
                    String.format(InventoryConstants.INVALID_QUALITY_AVAILABLE_QUANTITY_MESSAGE,
                            statusSet.qualityStatus().name(), statusSet.qualityStatus().getDescription()));
        }
    }
}
