package com.kb.cosmetic_wms.product.category.domain.exception;

import com.kb.cosmetic_wms.global.error.BusinessException;

public class CategoryInUseException extends BusinessException {
    public CategoryInUseException() {
        super(CategoryErrorCode.CATEGORY_IN_USE);
    }
}
