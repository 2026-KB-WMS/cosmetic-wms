package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.constants.InventoryConstants;
import com.kb.cosmetic_wms.domain.inventory.enums.TransactionType;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InventoryTransaction extends BaseEntity {

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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "allocStatus", column = @Column(name = "prev_alloc_status", length = 20)),
            @AttributeOverride(name = "qualityStatus", column = @Column(name = "prev_quality_status", length = 20)),
            @AttributeOverride(name = "locStatus", column = @Column(name = "prev_loc_status", length = 20))
    })
    private InventoryStatusSet prevStatusSet;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "allocStatus", column = @Column(name = "curr_alloc_status", length = 20, nullable = false)),
            @AttributeOverride(name = "qualityStatus", column = @Column(name = "curr_quality_status", length = 20, nullable = false)),
            @AttributeOverride(name = "locStatus", column = @Column(name = "curr_loc_status", length = 20, nullable = false))
    })
    private InventoryStatusSet currStatusSet;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Builder(access = AccessLevel.PRIVATE)
    private InventoryTransaction(
            TransactionType transactionType, int transactionQuantity, int balanceQuantity,
            Long referenceId, InventoryStatusSet prevStatusSet, InventoryStatusSet currStatusSet,
            String changeReason, Long inventoryId, Long memberId
    ) {
        this.transactionType = transactionType;
        this.transactionQuantity = transactionQuantity;
        this.balanceQuantity = balanceQuantity;
        this.referenceId = referenceId;
        this.prevStatusSet = prevStatusSet;
        this.currStatusSet = currStatusSet;
        this.changeReason = changeReason;
        this.inventoryId = inventoryId;
        this.memberId = memberId;
    }

    public static InventoryTransaction create(
            Long inventoryId, TransactionType transactionType, int transactionQuantity, int balanceQuantity,
            Long referenceId, InventoryStatusSet prevStatusSet, InventoryStatusSet currStatusSet,
            Long memberId, String changeReason
    ) {
        validate(inventoryId, transactionType, currStatusSet, memberId, transactionQuantity, referenceId);

        return InventoryTransaction.builder()
                .inventoryId(inventoryId)
                .transactionType(transactionType)
                .transactionQuantity(transactionQuantity)
                .balanceQuantity(balanceQuantity)
                .referenceId(referenceId)
                .prevStatusSet(prevStatusSet)
                .currStatusSet(currStatusSet)
                .memberId(memberId)
                .changeReason(changeReason)
                .build();
    }

    private static void validate(
            Long inventoryId, TransactionType transactionType, InventoryStatusSet currStatusSet,
            Long memberId, int transactionQuantity, Long referenceId
    ) {
        if (inventoryId == null) {
            throw new IllegalArgumentException(InventoryConstants.INVENTORY_ID_REQUIRED_MESSAGE);
        }
        if (transactionType == null) {
            throw new IllegalArgumentException(InventoryConstants.TRANSACTION_TYPE_REQUIRED_MESSAGE);
        }
        if (currStatusSet == null) {
            throw new IllegalArgumentException(InventoryConstants.CURR_STATUS_REQUIRED_MESSAGE);
        }
        if (memberId == null) {
            throw new IllegalArgumentException(InventoryConstants.MEMBER_ID_REQUIRED_MESSAGE);
        }

        if (transactionQuantity <= 0) {
            throw new IllegalArgumentException(InventoryConstants.INVALID_TRANSACTION_QTY_MESSAGE);
        }

        if (transactionType.isReferenceRequired() && referenceId == null) {
            throw new IllegalArgumentException(
                    String.format(InventoryConstants.REFERENCE_ID_REQUIRED_TEMPLATE, transactionType.getDescription())
            );
        }
    }
}
