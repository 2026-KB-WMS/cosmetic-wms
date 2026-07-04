package com.kb.cosmetic_wms.inventory.application.service;

import com.kb.cosmetic_wms.inventory.application.port.in.*;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import com.kb.cosmetic_wms.inventory.application.port.out.SectionAssignmentPort;
import com.kb.cosmetic_wms.inventory.domain.enums.AllocStatus;
import java.time.LocalDate;
import com.kb.cosmetic_wms.inventory.domain.enums.LocStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.QualityStatus;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;
import com.kb.cosmetic_wms.inventory.domain.model.SplitResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService implements
        FindInventoryUseCase,
        ManageInventoryStatusUseCase,
        ApplyInspectionResultUseCase,
        DeductInventoryForOutboundUseCase,
        ReleaseInventoryForOutboundUseCase,
        FindFefoInventoryUseCase,
        FindProductAvailabilityUseCase {

    private final InventoryPort inventoryPort;
    private final InventoryTransactionPort inventoryTransactionPort;
    private final SectionAssignmentPort sectionAssignmentPort;


    @Override
    public InventoryResult findById(Long inventoryId) {
        Inventory inventory = inventoryPort.findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);
        return InventoryResult.from(inventory);
    }

    @Override
    public List<InventoryResult> findByLotId(Long lotId) {
        return inventoryPort.findByLotId(lotId).stream()
                .map(InventoryResult::from)
                .toList();
    }

    @Override
    public List<InventoryResult> findByProductId(Long productId) {
        return inventoryPort.findByProductId(productId).stream()
                .map(InventoryResult::from)
                .toList();
    }

    @Override
    @Transactional
    public InventoryResult allocate(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.allocate(command.quantity()),
                TransactionType.ALLOCATE, command.quantity(), command.referenceId(), command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult unallocate(Long inventoryId, InventoryStatusChangeCommand command) {
        return doUnallocate(inventoryId, command);
    }

    @Override
    @Transactional
    public InventoryResult startMoving(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.startMoving(command.quantity()),
                TransactionType.LOCATION_MOVE, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult finishMoving(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.finishMoving(command.quantity()),
                TransactionType.LOCATION_MOVE, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult startInspecting(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.startInspecting(command.quantity()),
                TransactionType.QUALITY_INSPECTING, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult restoreToNormalQuality(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.restoreToNormalQuality(command.quantity()),
                TransactionType.QUALITY_RELEASE, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult holdForQualityIssue(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.holdForQualityIssue(command.quantity()),
                TransactionType.QUALITY_HOLD, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public InventoryResult scheduleForDiscard(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.scheduleForDiscard(command.quantity()),
                TransactionType.DISCARD, command.quantity(), null, command.memberId());
    }

    @Override
    @Transactional
    public void applyInspectionResult(InspectionResultCommand command) {
        SectionAssignmentPort.SectionAssignment assignment = sectionAssignmentPort.assignSectionsForInspection(
                command.warehouseId(), command.productId(), command.passedQuantity(), command.failedQuantity());

        if (command.passedQuantity() > 0) {
            InventoryStatusSet normalStatus = InventoryStatusSet.of(
                    AllocStatus.UNALLOCATED, QualityStatus.NORMAL, LocStatus.STORED);
            createOrMerge(command.productId(), command.lotId(), assignment.storageSectionId(), command.warehouseId(),
                    command.passedQuantity(), normalStatus, TransactionType.INSPECTION_PASS,
                    command.inspectionId(), command.memberId(), command.expiryDate());
        }
        if (command.failedQuantity() > 0) {
            InventoryStatusSet holdStatus = InventoryStatusSet.of(
                    AllocStatus.UNALLOCATED, QualityStatus.HOLD, LocStatus.STORED);
            createOrMerge(command.productId(), command.lotId(), assignment.quarantineSectionId(), command.warehouseId(),
                    command.failedQuantity(), holdStatus, TransactionType.INSPECTION_FAIL,
                    command.inspectionId(), command.memberId(), command.expiryDate());
        }
    }

    @Override
    @Transactional
    public void deductForOutbound(Long outboundId, Long memberId) {
        List<InventoryTransaction> allocations = inventoryTransactionPort
                .findByTransactionTypeAndReferenceId(TransactionType.ALLOCATE, outboundId);
        for (InventoryTransaction alloc : allocations) {
            Inventory inv = findOrThrow(alloc.getInventoryId());
            inv.deduct(alloc.getTransactionQuantity());
            inventoryTransactionPort.save(InventoryTransaction.create(
                    inv.getId(), TransactionType.SHIP, alloc.getTransactionQuantity(), inv.getQuantity(),
                    outboundId, inv.getStatusSet(), inv.getStatusSet(), memberId, null
            ));
            if (inv.isEmpty()) {
                inventoryPort.delete(inv);
            } else {
                inventoryPort.save(inv);
            }
        }
    }

    @Override
    @Transactional
    public void releaseForOutbound(Long outboundId, Long memberId) {
        List<InventoryTransaction> allocations = inventoryTransactionPort
                .findByTransactionTypeAndReferenceId(TransactionType.ALLOCATE, outboundId);
        for (InventoryTransaction alloc : allocations) {
            doUnallocate(alloc.getInventoryId(),
                    new InventoryStatusChangeCommand(alloc.getTransactionQuantity(), outboundId, memberId));
        }
    }

    private InventoryResult doUnallocate(Long inventoryId, InventoryStatusChangeCommand command) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.unallocate(command.quantity()),
                TransactionType.UNALLOCATE, command.quantity(), command.referenceId(), command.memberId());
    }

    @Override
    public List<FefoInventorySlice> findAvailableForFefo(Long productId, Long warehouseId) {
        return inventoryPort.findAvailableForFefo(productId, warehouseId);
    }

    @Override
    public List<ProductAvailabilitySlice> findAvailabilityByProducts(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return inventoryPort.findAvailabilityByProducts(productIds);
    }

    private Inventory findOrThrow(Long inventoryId) {
        return inventoryPort.findByIdForUpdate(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);
    }

    private InventoryResult applyAndRecord(
            Inventory inventory,
            Function<Inventory, SplitResult> operation,
            TransactionType type,
            int quantity,
            Long referenceId,
            Long memberId
    ) {
        InventoryStatusSet prevStatusSet = inventory.getStatusSet();
        SplitResult splitResult = operation.apply(inventory);
        boolean isSplit = splitResult.wasSplit();
        Inventory result = splitResult.result();

        result = persistSplitResult(inventory, result, isSplit);
        recordTransactions(inventory, result, isSplit, prevStatusSet, type, quantity, referenceId, memberId);

        return InventoryResult.from(result);
    }

    private Inventory persistSplitResult(Inventory inventory, Inventory result, boolean isSplit) {
        Optional<Inventory> mergeTarget = inventoryPort.findMergeTargetForUpdate(
                result.getProductId(), result.getLotId(), result.getSectionId(),
                result.getStatusSet(), inventory.getId()
        );

        if (mergeTarget.isPresent()) {
            Inventory target = mergeTarget.get();
            target.mergeFrom(result);
            if (isSplit) {
                inventoryPort.save(inventory);
            } else {
                inventoryPort.delete(inventory);
            }
            return inventoryPort.save(target);
        }

        if (isSplit) {
            inventoryPort.save(inventory);
            return inventoryPort.save(result);
        }

        return inventoryPort.save(result);
    }

    private void recordTransactions(
            Inventory inventory, Inventory result, boolean isSplit,
            InventoryStatusSet prevStatusSet, TransactionType type,
            int quantity, Long referenceId, Long memberId
    ) {
        if (isSplit) {
            inventoryTransactionPort.save(InventoryTransaction.create(
                    inventory.getId(), TransactionType.SPLIT_DEDUCT, quantity, inventory.getQuantity(),
                    null, prevStatusSet, inventory.getStatusSet(), memberId, null
            ));
        }
        inventoryTransactionPort.save(InventoryTransaction.create(
                result.getId(), type, quantity, result.getQuantity(),
                referenceId, prevStatusSet, result.getStatusSet(), memberId, null
        ));
    }

    private void createOrMerge(Long productId, Long lotId, Long sectionId, Long warehouseId,
                               int quantity, InventoryStatusSet statusSet,
                               TransactionType type, Long referenceId, Long memberId, LocalDate expiryDate) {
        int availableQty = (statusSet.qualityStatus().isNormal() && statusSet.locStatus() != LocStatus.DOCKING) ? quantity : 0;

        long NO_EXCLUDE_ID = -1L;
        Optional<Inventory> mergeTarget = inventoryPort.findMergeTargetForUpdate(
                productId, lotId, sectionId, statusSet, NO_EXCLUDE_ID
        );

        if (mergeTarget.isPresent()) {
            Inventory existing = mergeTarget.get();
            existing.mergeFrom(Inventory.create(productId, lotId, sectionId, warehouseId,
                    quantity, availableQty, statusSet, expiryDate));
            Inventory merged = inventoryPort.save(existing);
            inventoryTransactionPort.save(InventoryTransaction.create(
                    merged.getId(), type, quantity, merged.getQuantity(),
                    referenceId, statusSet, statusSet, memberId, null
            ));
            return;
        }

        Inventory saved = inventoryPort.save(
                Inventory.create(productId, lotId, sectionId, warehouseId, quantity, availableQty, statusSet, expiryDate)
        );
        inventoryTransactionPort.save(InventoryTransaction.create(
                saved.getId(), type, quantity, quantity,
                referenceId, null, statusSet, memberId, null
        ));
    }
}