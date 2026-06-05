package com.kb.cosmetic_wms.domain.order;

import com.kb.cosmetic_wms.domain.product.entity.Product;

public record OrderLine(Product product, int quantity) {
    public OrderLine {
        if (product == null) {
            throw new IllegalArgumentException("발주 항목의 상품 정보는 필수입니다.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("발주 수량은 0보다 커야 합니다.");
        }
    }
}
