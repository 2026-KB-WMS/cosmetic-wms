package com.kb.cosmetic_wms.domain.inventory.service;

import com.kb.cosmetic_wms.domain.inventory.dto.InventoryDetailResponseDto;
import com.kb.cosmetic_wms.domain.inventory.dto.InventoryStatusChangeRequestDto;
import com.kb.cosmetic_wms.domain.inventory.entity.Inventory;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryStatusSet;
import com.kb.cosmetic_wms.domain.inventory.entity.InventoryTransaction;
import com.kb.cosmetic_wms.domain.inventory.enums.TransactionType;
import com.kb.cosmetic_wms.domain.inventory.exception.InventoryNotFoundException;
import com.kb.cosmetic_wms.domain.inventory.repository.InventoryRepository;
import com.kb.cosmetic_wms.domain.inventory.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryDetailResponseDto getInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);
        return InventoryDetailResponseDto.from(inventory);
    }

    public List<InventoryDetailResponseDto> getInventoriesByLotId(Long lotId) {
        return inventoryRepository.findByLotId(lotId).stream()
                .map(InventoryDetailResponseDto::from)
                .toList();
    }

    public List<InventoryDetailResponseDto> getInventoriesByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId).stream()
                .map(InventoryDetailResponseDto::from)
                .toList();
    }

    @Transactional
    public InventoryDetailResponseDto allocate(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.allocate(request.quantity()),
                TransactionType.ALLOCATE, request.quantity(), request.referenceId(), request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto unallocate(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.unallocate(request.quantity()),
                TransactionType.UNALLOCATE, request.quantity(), request.referenceId(), request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto startMoving(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.startMoving(request.quantity()),
                TransactionType.LOCATION_MOVE, request.quantity(), null, request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto finishMoving(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.finishMoving(request.quantity()),
                TransactionType.LOCATION_MOVE, request.quantity(), null, request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto startInspecting(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.startInspecting(request.quantity()),
                TransactionType.QUALITY_INSPECTING, request.quantity(), null, request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto restoreToNormalQuality(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.restoreToNormalQuality(request.quantity()),
                TransactionType.QUALITY_RELEASE, request.quantity(), null, request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto holdForQualityIssue(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.holdForQualityIssue(request.quantity()),
                TransactionType.QUALITY_HOLD, request.quantity(), null, request.memberId());
    }

    @Transactional
    public InventoryDetailResponseDto scheduleForDiscard(Long inventoryId, InventoryStatusChangeRequestDto request) {
        Inventory inventory = findOrThrow(inventoryId);
        return applyAndRecord(inventory, inv -> inv.scheduleForDiscard(request.quantity()),
                TransactionType.DISCARD, request.quantity(), null, request.memberId());
    }

    private Inventory findOrThrow(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(InventoryNotFoundException::new);
    }

    private InventoryDetailResponseDto applyAndRecord(
            Inventory inventory,
            Function<Inventory, Inventory> operation,
            TransactionType type,
            int quantity,
            Long referenceId,
            Long memberId
    ) {
        InventoryStatusSet prevStatusSet = inventory.getStatusSet();
        Inventory result = operation.apply(inventory);
        boolean isSplit = (result != inventory);

        result = persistSplitResult(inventory, result, isSplit);
        recordTransactions(inventory, result, isSplit, prevStatusSet, type, quantity, referenceId, memberId);

        return InventoryDetailResponseDto.from(result);
    }

    /**
     * 분할 결과를 DB에 반영한다.
     *
     * - mergeTarget 존재: 기존 동일 상태 재고에 수량 합산. 전체 수량 변경(isSplit=false)으로
     *   원본 자체가 상태 변경된 경우 uk 충돌 방지를 위해 원본을 삭제한다.
     * - mergeTarget 없음 + isSplit=true: 분할된 새 재고를 저장한다.
     * - mergeTarget 없음 + isSplit=false: Dirty Checking이 UPDATE를 처리한다.
     */
    private Inventory persistSplitResult(Inventory inventory, Inventory result, boolean isSplit) {
        Optional<Inventory> mergeTarget = inventoryRepository.findMergeTarget(
                result.getProductId(), result.getLotId(), result.getSectionId(),
                result.getStatusSet(), inventory.getId()
        );

        if (mergeTarget.isPresent()) {
            mergeTarget.get().mergeFrom(result);
            if (!isSplit) {
                inventoryRepository.delete(inventory);
            }
            return mergeTarget.get();
        }

        if (isSplit) {
            return inventoryRepository.save(result);
        }

        return result;
    }

    /**
     * 오퍼레이션 이력을 기록한다.
     *
     * 분할(isSplit=true)이 발생한 경우 원본 재고의 수량 차감 이력(SPLIT_DEDUCT)을 먼저 기록하고,
     * 이후 실제 오퍼레이션 타입으로 결과 재고의 이력을 기록한다.
     */
    private void recordTransactions(
            Inventory inventory, Inventory result, boolean isSplit,
            InventoryStatusSet prevStatusSet, TransactionType type,
            int quantity, Long referenceId, Long memberId
    ) {
        if (isSplit) {
            inventoryTransactionRepository.save(InventoryTransaction.create(
                    inventory.getId(), TransactionType.SPLIT_DEDUCT, quantity, inventory.getQuantity(),
                    null, prevStatusSet, inventory.getStatusSet(), memberId, null
            ));
        }
        inventoryTransactionRepository.save(InventoryTransaction.create(
                result.getId(), type, quantity, result.getQuantity(),
                referenceId, prevStatusSet, result.getStatusSet(), memberId, null
        ));
    }
}
