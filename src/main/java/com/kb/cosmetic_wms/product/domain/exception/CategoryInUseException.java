package com.kb.cosmetic_wms.product.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class CategoryInUseException extends BusinessException {
    public CategoryInUseException() {
        super(ProductErrorCode.CATEGORY_IN_USE);
    }
}