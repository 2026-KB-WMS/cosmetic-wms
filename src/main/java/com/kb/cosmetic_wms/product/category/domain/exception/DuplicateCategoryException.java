package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateCategoryException extends BusinessException {
    public DuplicateCategoryException() {
        super(CategoryErrorCode.DUPLICATE_CATEGORY);
    }
}