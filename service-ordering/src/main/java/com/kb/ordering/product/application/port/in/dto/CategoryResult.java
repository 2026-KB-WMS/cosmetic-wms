package com.kb.ordering.product.application.port.in.dto;

import com.kb.ordering.product.domain.category.model.Category;

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