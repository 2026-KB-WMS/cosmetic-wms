package com.kb.ordering.product.adapter.out.persistence;

import com.kb.common.jpa.BaseEntity;
import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;
import com.kb.ordering.product.domain.product.model.ProductInfo;
import com.kb.ordering.product.domain.product.valueobject.Volume;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

@Entity
@Table(
        name = "product",
        uniqueConstraints = @UniqueConstraint(name = "uq_product_sku_code", columnNames = "sku_code")
)
@Check(name = "chk_product_price_non_negative", constraints = "product_price >= 0")
@Check(name = "chk_volume_positive", constraints = "volume > 0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "sku_code", length = 50)
    private String skuCode;

    @Column(name = "brand_name", nullable = false, length = 50)
    private String brandName;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private int productPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "temperature_type", nullable = false, length = 20)
    private TemperatureType temperatureType;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "type_id", nullable = false)
    private Long productTypeId;

    @Column(name = "skin_type", length = 50)
    private String skinType;

    @Column(name = "function_type", length = 100)
    private String functionType;

    @Column(name = "volume", nullable = false)
    private int volumeValue;

    @Column(name = "unit", nullable = false, length = 10)
    private String volumeUnit;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String cautions;

    @Column(name = "storage_condition", length = 255)
    private String storageCondition;

    private ProductEntity(String skuCode, String brandName, String productName, int productPrice,
                          TemperatureType temperatureType, Long categoryId, Long productTypeId,
                          String skinType, String functionType,
                          int volumeValue, String volumeUnit,
                          String ingredients, String cautions, String storageCondition) {
        this.skuCode = skuCode;
        this.brandName = brandName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.temperatureType = temperatureType;
        this.categoryId = categoryId;
        this.productTypeId = productTypeId;
        this.skinType = skinType;
        this.functionType = functionType;
        this.volumeValue = volumeValue;
        this.volumeUnit = volumeUnit;
        this.ingredients = ingredients;
        this.cautions = cautions;
        this.storageCondition = storageCondition;
    }

    static ProductEntity fromDomain(Product product) {
        ProductInfo info = product.getProductInfo();
        ProductEntity entity = new ProductEntity(
                product.getSkuCode(), product.getBrandName(), product.getProductName(),
                product.getProductPrice(), product.getTemperatureType(),
                product.getCategoryId(), product.getProductTypeId(),
                info.getSkinType(), info.getFunctionType(),
                info.getVolume().value(), info.getVolume().unit(),
                info.getIngredients(), info.getCautions(), info.getStorageCondition()
        );
        entity.id = product.getProductId();
        return entity;
    }

    Product toDomain() {
        Volume volume = Volume.of(volumeValue, volumeUnit);
        ProductInfo productInfo = ProductInfo.reconstitute(
                skinType, functionType, volume, ingredients, cautions, storageCondition
        );
        return Product.reconstitute(
                id, skuCode, brandName, productName, productPrice, temperatureType,
                categoryId, productTypeId, productInfo
        );
    }
}