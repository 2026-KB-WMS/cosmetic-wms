package com.kb.cosmetic_wms.product.adapter.in.web;

import com.kb.cosmetic_wms.product.application.port.in.CategoryResult;

public record CategoryResponse(
        Long categoryId,
        String categoryCode,
        String categoryName
) {
    public static CategoryResponse from(CategoryResult result) {
        return new CategoryResponse(result.categoryId(), result.categoryCode(), result.categoryName());
    }
}