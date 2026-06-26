package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidCategoryCodeException extends BusinessException {
    public InvalidCategoryCodeException(String detailMessage) {
        super(CategoryErrorCode.INVALID_CATEGORY_CODE, detailMessage);
    }
}