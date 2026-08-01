package com.kb.ordering.product.domain.category.exception;

import com.kb.common.error.BusinessException;

public class InvalidCategoryNameException extends BusinessException {
    public InvalidCategoryNameException() {
        super(CategoryErrorCode.INVALID_CATEGORY_NAME);
    }
}