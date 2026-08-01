package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.ProductTypeResult;
import com.kb.ordering.product.application.port.in.dto.RegisterProductTypeCommand;

public interface RegisterProductTypeUseCase {
    ProductTypeResult register(RegisterProductTypeCommand command);
}
