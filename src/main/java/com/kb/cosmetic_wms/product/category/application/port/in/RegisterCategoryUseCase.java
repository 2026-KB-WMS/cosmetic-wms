package com.kb.cosmetic_wms.product.category.application.port.in;

public interface RegisterCategoryUseCase {
    CategoryResult register(RegisterCategoryCommand command);
}