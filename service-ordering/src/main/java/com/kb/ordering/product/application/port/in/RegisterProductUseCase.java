package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.ProductResult;
import com.kb.ordering.product.application.port.in.dto.RegisterProductCommand;

public interface RegisterProductUseCase {
    ProductResult register(RegisterProductCommand command);
}