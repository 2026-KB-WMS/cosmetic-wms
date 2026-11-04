package com.kb.cosmetic_wms.inventory.domain.model;

import com.kb.cosmetic_wms.inventory.domain.exception.InvalidShelfLifePolicyException;

import java.time.LocalDate;

/**
 * 출고할 수 있는 최소 잔여 유통기한 정책. 잔여일이 {@code minRemainingDays} 이하인 재고는 출고하지 않는다.
 */
public record ShelfLifePolicy(int minRemainingDays) {

    public ShelfLifePolicy {
        if (minRemainingDays < 0) {
            throw new InvalidShelfLifePolicyException();
        }
    }

    /**
     * 이 날짜보다 유통기한이 늦은 재고만 출고할 수 있다.
     */
    public LocalDate allocatableAfter(LocalDate today) {
        return today.plusDays(minRemainingDays);
    }

    public boolean isAllocatable(LocalDate expiryDate, LocalDate today) {
        return expiryDate == null || expiryDate.isAfter(allocatableAfter(today));
    }
}
