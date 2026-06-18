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

    /**
     * 도메인 오퍼레이션을 적용하고 트랜잭션 이력을 기록한다.
     * 부분 분할(split) 발생 시 새 재고를 먼저 저장하여 ID를 확보한 뒤 이력에 연결한다.
     */
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
        if (isSplit) {
            result = inventoryRepository.save(result);
        }

        Long recordedInventoryId = isSplit ? result.getId() : inventory.getId();

        inventoryTransactionRepository.save(InventoryTransaction.create(
                recordedInventoryId, type, quantity, result.getQuantity(),
                referenceId, prevStatusSet, result.getStatusSet(), memberId, null
        ));

        return InventoryDetailResponseDto.from(result);
    }
}
