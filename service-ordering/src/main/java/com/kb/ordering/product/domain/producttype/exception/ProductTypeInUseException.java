package com.kb.ordering.product.domain.producttype.exception;

import com.kb.common.error.BusinessException;

public class ProductTypeInUseException extends BusinessException {
    public ProductTypeInUseException() {
        super(ProductTypeErrorCode.PRODUCT_TYPE_IN_USE);
    }
}