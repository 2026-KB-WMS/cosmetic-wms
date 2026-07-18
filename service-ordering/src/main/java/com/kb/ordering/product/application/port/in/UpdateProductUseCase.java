package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.ProductResult;
import com.kb.ordering.product.application.port.in.dto.UpdateProductCommand;

public interface UpdateProductUseCase {
    ProductResult update(Long productId, UpdateProductCommand command);
}