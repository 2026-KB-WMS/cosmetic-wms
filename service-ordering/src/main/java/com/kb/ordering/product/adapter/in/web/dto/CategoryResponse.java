package com.kb.ordering.product.adapter.in.web.dto;

import com.kb.ordering.product.application.port.in.dto.CategoryResult;

public record CategoryResponse(
        Long categoryId,
        String categoryCode,
        String categoryName
) {
    public static CategoryResponse from(CategoryResult result) {
        return new CategoryResponse(result.categoryId(), result.categoryCode(), result.categoryName());
    }
}