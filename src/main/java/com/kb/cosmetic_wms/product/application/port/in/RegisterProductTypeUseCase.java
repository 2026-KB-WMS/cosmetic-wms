package com.kb.cosmetic_wms.product.application.port.in;

public interface RegisterProductTypeUseCase {
    ProductTypeResult register(RegisterProductTypeCommand command);
}