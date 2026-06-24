package com.kb.cosmetic_wms.product.application.port.in;

import com.kb.cosmetic_wms.product.domain.model.Category;

public record CategoryResult(
        Long categoryId,
        String categoryCode,
        String categoryName
) {
    public static CategoryResult from(Category category) {
        return new CategoryResult(
                category.getCategoryId(),
                category.getCategoryCode(),
                category.getCategoryName()
        );
    }
}
