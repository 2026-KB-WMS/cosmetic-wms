package com.kb.cosmetic_wms.product.application.port.in;

public record RegisterCategoryCommand(
        String categoryCode,
        String categoryName
) {
}
