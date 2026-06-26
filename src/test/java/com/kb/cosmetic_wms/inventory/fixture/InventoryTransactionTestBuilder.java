package com.kb.cosmetic_wms.inventory.fixture;

import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;

public class InventoryTransactionTestBuilder {
    private Long inventoryId = 100L;
    private TransactionType transactionType = TransactionType.LOCATION_MOVE;
    private int transactionQuantity = 10;
    private int balanceQuantity = 100;
    private Long referenceId = null;

    private InventoryStatusSet prevStatusSet = InventoryStatusSet.of(
            AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
    );
    private InventoryStatusSet currStatusSet = InventoryStatusSet.of(
            AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED
    );

    private String changeReason = "테스트 목적의 재고 변동";
    private Long memberId = 1L;

    public InventoryTransactionTestBuilder inventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
        return this;
    }

    public InventoryTransactionTestBuilder transactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public InventoryTransactionTestBuilder transactionQuantity(int transactionQuantity) {
        this.transactionQuantity = transactionQuantity;
        return this;
    }

    public InventoryTransactionTestBuilder balanceQuantity(int balanceQuantity) {
        this.balanceQuantity = balanceQuantity;
        return this;
    }

    public InventoryTransactionTestBuilder referenceId(Long referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    public InventoryTransactionTestBuilder prevStatusSet(InventoryStatusSet prevStatusSet) {
        this.prevStatusSet = prevStatusSet;
        return this;
    }

    public InventoryTransactionTestBuilder currStatusSet(InventoryStatusSet currStatusSet) {
        this.currStatusSet = currStatusSet;
        return this;
    }

    public InventoryTransactionTestBuilder changeReason(String changeReason) {
        this.changeReason = changeReason;
        return this;
    }

    public InventoryTransactionTestBuilder memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public InventoryTransactionTestBuilder nullPrevStatusSet() {
        this.prevStatusSet = null;
        return this;
    }

    public InventoryTransactionTestBuilder nullReferenceId() {
        this.referenceId = null;
        return this;
    }

    public InventoryTransaction build() {
        return InventoryTransaction.create(
                this.inventoryId,
                this.transactionType,
                this.transactionQuantity,
                this.balanceQuantity,
                this.referenceId,
                this.prevStatusSet,
                this.currStatusSet,
                this.memberId,
                this.changeReason
        );
    }
}