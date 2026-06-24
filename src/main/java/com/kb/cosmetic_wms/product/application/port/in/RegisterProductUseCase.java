package com.kb.cosmetic_wms.product.application.port.in;

public interface RegisterProductUseCase {
    ProductResult register(RegisterProductCommand command);
}