package com.kb.cosmetic_wms.product.domain.model;

import com.kb.cosmetic_wms.product.domain.constants.ProductTypeConstants;
import lombok.Getter;

@Getter
public class ProductType {

    private final Long productTypeId;
    private final String typeCode;
    private final String typeName;

    private ProductType(Long productTypeId, String typeCode, String typeName) {
        this.productTypeId = productTypeId;
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public static ProductType create(String typeCode, String typeName) {
        validateTypeCode(typeCode);
        validateTypeName(typeName);
        return new ProductType(null, typeCode, typeName);
    }

    public static ProductType reconstitute(Long productTypeId, String typeCode, String typeName) {
        return new ProductType(productTypeId, typeCode, typeName);
    }

    private static void validateTypeCode(String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new IllegalArgumentException(ProductTypeConstants.PRODUCT_TYPE_CODE_REQUIRED_MESSAGE);
        }
        if (typeCode.length() != ProductTypeConstants.PRODUCT_TYPE_CODE_LENGTH) {
            throw new IllegalArgumentException(ProductTypeConstants.INVALID_PRODUCT_TYPE_CODE_LENGTH_MESSAGE);
        }
        if (!ProductTypeConstants.PRODUCT_TYPE_CODE_PATTERN.matcher(typeCode).matches()) {
            throw new IllegalArgumentException(ProductTypeConstants.INVALID_PRODUCT_TYPE_CODE_FORMAT_MESSAGE);
        }
    }

    private static void validateTypeName(String typeName) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException(ProductTypeConstants.PRODUCT_TYPE_NAME_REQUIRED_MESSAGE);
        }
    }
}