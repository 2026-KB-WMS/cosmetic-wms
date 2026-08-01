package com.kb.ordering.product.domain.producttype.model;

import com.kb.ordering.product.domain.producttype.exception.InvalidTypeNameException;
import com.kb.ordering.product.domain.producttype.valueobject.TypeCode;
import lombok.Getter;

@Getter
public class ProductType {

    private final Long productTypeId;
    @Getter(lombok.AccessLevel.NONE)
    private final TypeCode typeCode;
    private final String typeName;

    private ProductType(Long productTypeId, TypeCode typeCode, String typeName) {
        this.productTypeId = productTypeId;
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public static ProductType create(String typeCode, String typeName) {
        TypeCode code = new TypeCode(typeCode);
        if (typeName == null || typeName.isBlank()) {
            throw new InvalidTypeNameException();
        }
        return new ProductType(null, code, typeName);
    }

    public static ProductType reconstitute(Long productTypeId, String typeCode, String typeName) {
        return new ProductType(productTypeId, new TypeCode(typeCode), typeName);
    }

    public String getTypeCode() {
        return typeCode.value();
    }
}