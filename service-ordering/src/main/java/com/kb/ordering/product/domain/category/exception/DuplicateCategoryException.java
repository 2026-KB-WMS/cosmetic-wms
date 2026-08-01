package com.kb.ordering.product.domain.category.exception;

import com.kb.common.error.BusinessException;

public class DuplicateCategoryException extends BusinessException {
    public DuplicateCategoryException() {
        super(CategoryErrorCode.DUPLICATE_CATEGORY);
    }
}