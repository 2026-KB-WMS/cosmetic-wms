package com.kb.cosmetic_wms.product.category.application.port.in;

public record RegisterCategoryCommand(
        String categoryCode,
        String categoryName
) {
}