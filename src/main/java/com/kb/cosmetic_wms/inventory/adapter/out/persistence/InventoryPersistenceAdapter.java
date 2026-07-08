package com.kb.cosmetic_wms.inventory.adapter.out.persistence;

import com.kb.cosmetic_wms.inventory.application.port.out.InventoryPort;
import com.kb.cosmetic_wms.inventory.application.port.out.InventoryTransactionPort;
import com.kb.cosmetic_wms.inventory.domain.enums.TransactionType;
import com.kb.cosmetic_wms.inventory.domain.model.Inventory;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryStatusSet;
import com.kb.cosmetic_wms.inventory.domain.model.InventoryTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryPersistenceAdapter implements InventoryPort, InventoryTransactionPort {

    private final InventoryJpaRepository inventoryJpaRepository;
    private final InventoryTransactionJpaRepository inventoryTransactionJpaRepository;

    @Override
    public Optional<Inventory> findById(Long id) {
        return inventoryJpaRepository.findById(id).map(InventoryEntity::toDomain);
    }

    @Override
    public List<Inventory> findByLotId(Long lotId) {
        return inventoryJpaRepository.findByLotId(lotId).stream()
                .map(InventoryEntity::toDomain).toList();
    }

    @Override
    public List<Inventory> findByProductId(Long productId) {
        return inventoryJpaRepository.findByProductId(productId).stream()
                .map(InventoryEntity::toDomain).toList();
    }

    @Override
    public Optional<Inventory> findByIdForUpdate(Long id) {
        return inventoryJpaRepository.findByIdForUpdate(id).map(InventoryEntity::toDomain);
    }

    @Override
    public Optional<Inventory> findMergeTargetForUpdate(
            Long productId, Long lotId, Long sectionId, InventoryStatusSet statusSet, Long excludeId) {
        return inventoryJpaRepository.findMergeTargetForUpdate(
                productId, lotId, sectionId,
                statusSet.allocStatus(), statusSet.qualityStatus(), statusSet.locStatus(),
                excludeId
        ).map(InventoryEntity::toDomain);
    }

    @Override
    public List<FefoInventoryView> findAvailableForFefo(Long productId, Long warehouseId) {
        return inventoryJpaRepository.findAvailableForFefo(productId, warehouseId).stream()
                .map(row -> new FefoInventoryView(row.getInventoryId(), row.getAvailableQuantity()))
                .toList();
    }

    @Override
    public List<ProductAvailabilityView> findAvailabilityByProducts(Collection<Long> productIds) {
        return inventoryJpaRepository.findAvailabilityByProducts(productIds).stream()
                .map(row -> new ProductAvailabilityView(
                        row.getWarehouseId(),
                        row.getProductId(),
                        row.getAvailableQuantity(),
                        row.getEarliestExpiryDate()
                ))
                .toList();
    }

    @Override
    public Inventory save(Inventory inventory) {
        return inventoryJpaRepository.save(InventoryEntity.fromDomain(inventory)).toDomain();
    }

    @Override
    public void delete(Inventory inventory) {
        inventoryJpaRepository.deleteById(inventory.getId());
    }

    @Override
    public InventoryTransaction save(InventoryTransaction transaction) {
        return inventoryTransactionJpaRepository
                .save(InventoryTransactionEntity.fromDomain(transaction)).toDomain();
    }

    @Override
    public List<InventoryTransaction> findByTransactionTypeAndReferenceId(
            TransactionType type, Long referenceId) {
        return inventoryTransactionJpaRepository
                .findByTransactionTypeAndReferenceId(type, referenceId).stream()
                .map(InventoryTransactionEntity::toDomain).toList();
    }
}
