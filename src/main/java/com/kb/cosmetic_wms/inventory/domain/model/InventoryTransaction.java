package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryErrorCode;
import com.kb.cosmetic_wms.inventory.domain.exception.InvalidInventoryTransactionException;
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
            throw new InvalidInventoryTransactionException(InventoryErrorCode.TRANSACTION_INVENTORY_ID_REQUIRED);
        }
        if (transactionType == null) {
            throw new InvalidInventoryTransactionException(InventoryErrorCode.TRANSACTION_TYPE_REQUIRED);
        }
        if (currStatusSet == null) {
            throw new InvalidInventoryTransactionException(InventoryErrorCode.TRANSACTION_STATUS_REQUIRED);
        }
        if (memberId == null) {
            throw new InvalidInventoryTransactionException(InventoryErrorCode.TRANSACTION_MEMBER_ID_REQUIRED);
        }
        if (transactionQuantity <= 0) {
            throw new InvalidInventoryTransactionException(InventoryErrorCode.TRANSACTION_INVALID_QUANTITY);
        }
        if (transactionType.isReferenceRequired() && referenceId == null) {
            throw new InvalidInventoryTransactionException(
                    InventoryErrorCode.TRANSACTION_REFERENCE_REQUIRED,
                    transactionType.getDescription() + " 행위는 원인 전표 ID가 필수입니다."
            );
        }
    }
}
