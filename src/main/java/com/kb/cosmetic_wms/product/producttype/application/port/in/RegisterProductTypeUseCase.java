package com.kb.cosmetic_wms.product.producttype.application.port.in;

public interface RegisterProductTypeUseCase {
    ProductTypeResult register(RegisterProductTypeCommand command);
}
