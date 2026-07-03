package com.kb.cosmetic_wms.oms.domain.model;

public record DemandLine(Long productId, int quantity) {

    public DemandLine {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("요청 수량은 0보다 커야 합니다: " + quantity);
        }
    }
}
