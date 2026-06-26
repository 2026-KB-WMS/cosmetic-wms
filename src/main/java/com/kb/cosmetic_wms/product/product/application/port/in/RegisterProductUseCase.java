package com.kb.cosmetic_wms.product.product.application.port.in;

public interface RegisterProductUseCase {
    ProductResult register(RegisterProductCommand command);
}