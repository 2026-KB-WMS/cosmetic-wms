package com.kb.ordering.product.domain.category.exception;

import com.kb.common.error.BusinessException;

public class InvalidCategoryCodeException extends BusinessException {
    public InvalidCategoryCodeException(String detailMessage) {
        super(CategoryErrorCode.INVALID_CATEGORY_CODE, detailMessage);
    }
}