package com.kb.ordering.product.domain.category.exception;

import com.kb.common.error.BusinessException;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }
}