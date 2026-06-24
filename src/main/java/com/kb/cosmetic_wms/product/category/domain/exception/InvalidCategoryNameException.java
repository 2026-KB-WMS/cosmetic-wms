package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class InvalidCategoryNameException extends BusinessException {
    public InvalidCategoryNameException() {
        super(CategoryErrorCode.INVALID_CATEGORY_NAME);
    }
}