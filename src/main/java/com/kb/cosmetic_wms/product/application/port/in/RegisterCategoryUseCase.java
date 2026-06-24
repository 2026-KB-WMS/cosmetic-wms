package com.kb.cosmetic_wms.product.application.port.in;

public interface RegisterCategoryUseCase {
    CategoryResult register(RegisterCategoryCommand command);
}