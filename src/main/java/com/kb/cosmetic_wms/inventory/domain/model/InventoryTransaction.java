package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import lombok.Getter;

@Getter
public class InventoryTransaction {

    private Long id;
    private final Long inventoryId;
    private final TransactionType transactionType;
    private final int transactionQuantity;
    private final int balanceQuantity;
    private final Long referenceId;
    private final InventoryStatusSet prevStatusSet;
    private final InventoryStatusSet currStatusSet;
    private final Long memberId;
    private final String changeReason;

    private InventoryTransaction(
            Long id, Long inventoryId, TransactionType transactionType,
            int transactionQuantity, int balanceQuantity, Long referenceId,
            InventoryStatusSet prevStatusSet, InventoryStatusSet currStatusSet,
            Long memberId, String changeReason
    ) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.transactionType = transactionType;
        this.transactionQuantity = transactionQuantity;
        this.balanceQuantity = balanceQuantity;
        this.referenceId = referenceId;
        this.prevStatusSet = prevStatusSet;
        this.currStatusSet = currStatusSet;
        this.memberId = memberId;
        this.changeReason = changeReason;
    }

    public static InventoryTransaction create(
            Long inventoryId, TransactionType transactionType, int transactionQuantity, int balanceQuantity,
            Long referenceId, InventoryStatusSet prevStatusSet, InventoryStatusSet currStatusSet,
            Long memberId, String changeReason
    ) {
        validate(inventoryId, transactionType, currStatusSet, memberId, transactionQuantity, referenceId);

        return new InventoryTransaction(null, inventoryId, transactionType, transactionQuantity, balanceQuantity,
                referenceId, prevStatusSet, currStatusSet, memberId, changeReason);
    }

    public static InventoryTransaction reconstitute(
            Long id, Long inventoryId, TransactionType transactionType,
            int transactionQuantity, int balanceQuantity, Long referenceId,
            InventoryStatusSet prevStatusSet, InventoryStatusSet currStatusSet,
            Long memberId, String changeReason
    ) {
        return new InventoryTransaction(id, inventoryId, transactionType, transactionQuantity, balanceQuantity,
                referenceId, prevStatusSet, currStatusSet, memberId, changeReason);
    }

    private static void validate(
            Long inventoryId, TransactionType transactionType, InventoryStatusSet currStatusSet,
            Long memberId, int transactionQuantity, Long referenceId
    ) {
        if (inventoryId == null) {
            throw new IllegalArgumentException("재고 식별자(ID)는 필수입니다.");
        }
        if (transactionType == null) {
            throw new IllegalArgumentException("트랜잭션 타입은 필수입니다.");
        }
        if (currStatusSet == null) {
            throw new IllegalArgumentException("현재 재고 상태 정보는 필수입니다.");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("작업자 식별자(ID)는 필수입니다.");
        }
        if (transactionQuantity <= 0) {
            throw new IllegalArgumentException("트랜잭션 변동 수량은 0보다 커야 합니다.");
        }
        if (transactionType.isReferenceRequired() && referenceId == null) {
            throw new IllegalArgumentException(
                    String.format("%s 행위는 원인 전표 ID가 필수입니다.", transactionType.getDescription())
            );
        }
    }
}
