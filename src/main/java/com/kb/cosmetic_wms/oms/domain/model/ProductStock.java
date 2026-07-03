package com.kb.cosmetic_wms.oms.domain.model;

import java.time.LocalDate;

public record ProductStock(int availableQuantity, LocalDate earliestExpiryDate) {

    public ProductStock {
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("가용 수량은 0 이상이어야 합니다: " + availableQuantity);
        }
        if (availableQuantity > 0 && earliestExpiryDate == null) {
            throw new IllegalArgumentException("가용 재고가 있으면 최근접 유통기한은 필수입니다.");
        }
    }

    public static ProductStock empty() {
        return new ProductStock(0, null);
    }
}
