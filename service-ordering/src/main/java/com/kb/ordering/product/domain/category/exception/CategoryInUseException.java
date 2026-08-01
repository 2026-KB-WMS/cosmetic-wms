package com.kb.ordering.product.domain.category.exception;

import com.kb.common.error.BusinessException;

public class CategoryInUseException extends BusinessException {
    public CategoryInUseException() {
        super(CategoryErrorCode.CATEGORY_IN_USE);
    }
}
