package com.kb.cosmetic_wms.product.producttype.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductTypeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long id;

    @Column(name = "type_code", nullable = false, columnDefinition = "CHAR(3)")
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName;

    private ProductTypeEntity(String typeCode, String typeName) {
        this.typeCode = typeCode;
        this.typeName = typeName;
    }

    public static ProductTypeEntity fromDomain(ProductType productType) {
        return new ProductTypeEntity(productType.getTypeCode(), productType.getTypeName());
    }

    public ProductType toDomain() {
        return ProductType.reconstitute(id, typeCode, typeName);
    }
}
