package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.CategoryResult;
import com.kb.ordering.product.application.port.in.dto.RegisterCategoryCommand;

public interface RegisterCategoryUseCase {
    CategoryResult register(RegisterCategoryCommand command);
}