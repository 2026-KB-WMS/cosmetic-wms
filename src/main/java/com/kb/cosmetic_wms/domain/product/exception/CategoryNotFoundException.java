package com.kb.cosmetic_wms.domain.product.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super(ProductErrorCode.CATEGORY_NOT_FOUND);
    }
}
