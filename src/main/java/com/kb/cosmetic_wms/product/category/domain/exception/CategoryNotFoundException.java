package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super(CategoryErrorCode.CATEGORY_NOT_FOUND);
    }
}