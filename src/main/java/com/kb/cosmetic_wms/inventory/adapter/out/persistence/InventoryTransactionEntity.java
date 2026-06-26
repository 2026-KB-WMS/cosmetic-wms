package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class InventoryTransactionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20, nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_qty", nullable = false)
    private int transactionQuantity;

    @Column(name = "balance_qty", nullable = false)
    private int balanceQuantity;

    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_alloc_status", length = 20)
    private AllocStatus prevAllocStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_quality_status", length = 20)
    private QualityStatus prevQualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "prev_loc_status", length = 20)
    private LocStatus prevLocStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "curr_alloc_status", length = 20, nullable = false)
    private AllocStatus currAllocStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "curr_quality_status", length = 20, nullable = false)
    private QualityStatus currQualityStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "curr_loc_status", length = 20, nullable = false)
    private LocStatus currLocStatus;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    private InventoryTransactionEntity(
            Long id, Long inventoryId, TransactionType transactionType,
            int transactionQuantity, int balanceQuantity, Long referenceId,
            AllocStatus prevAllocStatus, QualityStatus prevQualityStatus, LocStatus prevLocStatus,
            AllocStatus currAllocStatus, QualityStatus currQualityStatus, LocStatus currLocStatus,
            Long memberId, String changeReason
    ) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.transactionType = transactionType;
        this.transactionQuantity = transactionQuantity;
        this.balanceQuantity = balanceQuantity;
        this.referenceId = referenceId;
        this.prevAllocStatus = prevAllocStatus;
        this.prevQualityStatus = prevQualityStatus;
        this.prevLocStatus = prevLocStatus;
        this.currAllocStatus = currAllocStatus;
        this.currQualityStatus = currQualityStatus;
        this.currLocStatus = currLocStatus;
        this.memberId = memberId;
        this.changeReason = changeReason;
    }

    static InventoryTransactionEntity fromDomain(InventoryTransaction tx) {
        InventoryStatusSet prev = tx.getPrevStatusSet();
        InventoryStatusSet curr = tx.getCurrStatusSet();
        return new InventoryTransactionEntity(
                tx.getId(),
                tx.getInventoryId(),
                tx.getTransactionType(),
                tx.getTransactionQuantity(),
                tx.getBalanceQuantity(),
                tx.getReferenceId(),
                prev != null ? prev.allocStatus() : null,
                prev != null ? prev.qualityStatus() : null,
                prev != null ? prev.locStatus() : null,
                curr.allocStatus(),
                curr.qualityStatus(),
                curr.locStatus(),
                tx.getMemberId(),
                tx.getChangeReason()
        );
    }

    InventoryTransaction toDomain() {
        InventoryStatusSet prevStatus = (prevAllocStatus != null)
                ? InventoryStatusSet.of(prevAllocStatus, prevQualityStatus, prevLocStatus)
                : null;
        InventoryStatusSet currStatus = InventoryStatusSet.of(currAllocStatus, currQualityStatus, currLocStatus);
        return InventoryTransaction.reconstitute(
                id, inventoryId, transactionType, transactionQuantity, balanceQuantity,
                referenceId, prevStatus, currStatus, memberId, changeReason
        );
    }
}