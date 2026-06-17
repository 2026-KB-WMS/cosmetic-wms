package com.kb.cosmetic_wms.domain.product.entity;

import com.kb.cosmetic_wms.domain.product.constants.ProductConstants;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
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
@Getter
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "sku_code", nullable = false, length = 50)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_category"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false, foreignKey = @ForeignKey(name = "fk_product_type"))
    private ProductType productType;

    @Embedded
    private ProductInfo productInfo;

    private Product(String brandName, String productName, int productPrice,
                    TemperatureType temperatureType, Category category,
                    ProductType productType, ProductInfo productInfo, int sequence
    ) {
        this.brandName = brandName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.temperatureType = temperatureType;
        this.category = category;
        this.productType = productType;
        this.productInfo = productInfo;

        this.skuCode = generateSkuCode(sequence);
    }

    public static Product create(String brandName, String productName, int productPrice,
                                 TemperatureType temperatureType, Category category,
                                 ProductType productType, ProductInfo productInfo, int sequence
    ) {
        validateBrandName(brandName);
        validateProductName(productName);
        validateProductPrice(productPrice);
        validateTemperatureType(temperatureType);
        validateRequiredObjects(category, productType, productInfo);
        validateSequence(sequence);

        return new Product(
                brandName, productName, productPrice, temperatureType,
                category, productType, productInfo, sequence
        );
    }

    public void update(String productName, int productPrice,
                       TemperatureType temperatureType, ProductInfo productInfo) {
        validateProductName(productName);
        validateProductPrice(productPrice);
        validateTemperatureType(temperatureType);
        validateRequiredObjects(this.category, this.productType, productInfo);

        this.productName = productName;
        this.productPrice = productPrice;
        this.temperatureType = temperatureType;
        this.productInfo = productInfo;
    }

    private String generateSkuCode(int sequence) {
        return String.format(ProductConstants.SKU_FORMAT,
                this.brandName.trim().toUpperCase(),
                this.category.getCategoryCode(),
                this.productType.getTypeCode(),
                this.productInfo.getVolume().value(),
                sequence
        );
    }

    private static void validateBrandName(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new IllegalArgumentException(ProductConstants.BRAND_NAME_REQUIRED_MESSAGE);
        }
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException(ProductConstants.PRODUCT_NAME_REQUIRED_MESSAGE);
        }
    }

    private static void validateProductPrice(int productPrice) {
        if (productPrice < ProductConstants.MIN_PRICE_BOUND) {
            throw new IllegalArgumentException(ProductConstants.INVALID_PRODUCT_PRICE_MESSAGE);
        }
    }

    private static void validateTemperatureType(TemperatureType temperatureType) {
        if (temperatureType == null) {
            throw new IllegalArgumentException(ProductConstants.TEMPERATURE_TYPE_REQUIRED_MESSAGE);
        }
    }

    private static void validateSequence(int sequence) {
        if (sequence < ProductConstants.SEQUENCE_MIN_BOUND) {
            throw new IllegalArgumentException(ProductConstants.INVALID_SEQUENCE_MIN_MESSAGE);
        }

        if (sequence > ProductConstants.SEQUENCE_MAX_BOUND) {
            throw new IllegalArgumentException(ProductConstants.INVALID_SEQUENCE_MAX_MESSAGE);
        }
    }

    private static void validateRequiredObjects(
            Category category, ProductType productType, ProductInfo productInfo
    ) {
        if (category == null) {
            throw new IllegalArgumentException(ProductConstants.CATEGORY_REQUIRED_MESSAGE);
        }
        if (productType == null) {
            throw new IllegalArgumentException(ProductConstants.PRODUCT_TYPE_REQUIRED_MESSAGE);
        }
        if (productInfo == null) {
            throw new IllegalArgumentException(ProductConstants.PRODUCT_INFO_REQUIRED_MESSAGE);
        }
    }
}
