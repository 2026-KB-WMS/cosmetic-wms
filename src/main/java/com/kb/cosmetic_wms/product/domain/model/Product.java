package com.kb.cosmetic_wms.product.domain.model;

import com.kb.cosmetic_wms.product.domain.constants.ProductConstants;
import com.kb.cosmetic_wms.product.domain.enums.TemperatureType;
import lombok.Getter;

@Getter
public class Product {

    private final Long productId;
    private String skuCode;
    private final String brandName;
    private String productName;
    private int productPrice;
    private TemperatureType temperatureType;
    private final Category category;
    private final ProductType productType;
    private ProductInfo productInfo;

    private Product(Long productId, String skuCode, String brandName, String productName,
                    int productPrice, TemperatureType temperatureType,
                    Category category, ProductType productType, ProductInfo productInfo) {
        this.productId = productId;
        this.skuCode = skuCode;
        this.brandName = brandName;
        this.productName = productName;
        this.productPrice = productPrice;
        this.temperatureType = temperatureType;
        this.category = category;
        this.productType = productType;
        this.productInfo = productInfo;
    }

    public static Product create(String brandName, String productName, int productPrice,
                                 TemperatureType temperatureType, Category category,
                                 ProductType productType, ProductInfo productInfo) {
        validateBrandName(brandName);
        validateProductName(productName);
        validateProductPrice(productPrice);
        validateTemperatureType(temperatureType);
        validateRequiredObjects(category, productType, productInfo);

        return new Product(null, null, brandName, productName, productPrice,
                temperatureType, category, productType, productInfo);
    }

    public static Product reconstitute(Long productId, String skuCode, String brandName, String productName,
                                       int productPrice, TemperatureType temperatureType,
                                       Category category, ProductType productType, ProductInfo productInfo) {
        return new Product(productId, skuCode, brandName, productName, productPrice,
                temperatureType, category, productType, productInfo);
    }

    public void assignSkuCode(Long productId) {
        this.skuCode = String.format(ProductConstants.SKU_FORMAT, productId);
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

    private static void validateRequiredObjects(Category category, ProductType productType, ProductInfo productInfo) {
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