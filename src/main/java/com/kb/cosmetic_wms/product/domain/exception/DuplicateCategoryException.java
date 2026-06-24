package com.kb.cosmetic_wms.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class DuplicateCategoryException extends BusinessException {
    public DuplicateCategoryException() {
        super(ProductErrorCode.DUPLICATE_CATEGORY);
    }
}