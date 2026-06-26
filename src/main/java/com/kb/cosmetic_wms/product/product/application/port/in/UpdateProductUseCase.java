package com.kb.cosmetic_wms.product.product.application.port.in;

public interface UpdateProductUseCase {
    ProductResult update(Long productId, UpdateProductCommand command);
}