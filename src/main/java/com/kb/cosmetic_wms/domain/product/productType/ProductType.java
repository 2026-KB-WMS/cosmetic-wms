package com.kb.cosmetic_wms.domain.product.productType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeCode;
    private String typeName;

    private ProductType(String typeCode, String typeName) {
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public static ProductType create(String typeCode, String typeName) {
        validateTypeCode(typeCode);
        validateTypeName(typeName);

        return new ProductType(typeCode, typeName);
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
