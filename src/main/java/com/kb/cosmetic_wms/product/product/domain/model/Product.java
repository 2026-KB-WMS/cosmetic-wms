package com.kb.cosmetic_wms.product.product.domain.model;

import com.kb.cosmetic_wms.product.category.domain.model.Category;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.exception.*;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Price;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import lombok.Getter;

@Getter
public class Product {

    public static final int BRAND_NAME_MAX_LENGTH = 100;
    public static final int PRODUCT_NAME_MAX_LENGTH = 255;
    private static final String SKU_FORMAT = "P%06d";

    private final Long productId;
    private String skuCode;
    private final String brandName;
    private String productName;
    @Getter(lombok.AccessLevel.NONE)
    private Price price;
    private TemperatureType temperatureType;
    private final Category category;
    private final ProductType productType;
    private ProductInfo productInfo;

    private Product(Long productId, String skuCode, String brandName, String productName,
                    Price price, TemperatureType temperatureType,
                    Category category, ProductType productType, ProductInfo productInfo) {
        this.productId = productId;
        this.skuCode = skuCode;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
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
        Price price = Price.of(productPrice);
        validateTemperatureType(temperatureType);
        validateRequiredObjects(category, productType, productInfo);

        return new Product(null, null, brandName, productName, price,
                temperatureType, category, productType, productInfo);
    }

    public static Product reconstitute(Long productId, String skuCode, String brandName, String productName,
                                       int productPrice, TemperatureType temperatureType,
                                       Category category, ProductType productType, ProductInfo productInfo) {
        return new Product(productId, skuCode, brandName, productName, Price.of(productPrice),
                temperatureType, category, productType, productInfo);
    }

    public void assignSkuCode(Long productId) {
        this.skuCode = String.format(SKU_FORMAT, productId);
    }

    public void update(String productName, int productPrice,
                       TemperatureType temperatureType, ProductInfo productInfo) {
        validateProductName(productName);
        Price newPrice = Price.of(productPrice);
        validateTemperatureType(temperatureType);
        validateRequiredObjects(this.category, this.productType, productInfo);

        this.productName = productName;
        this.price = newPrice;
        this.temperatureType = temperatureType;
        this.productInfo = productInfo;
    }

    public int getProductPrice() {
        return price.value();
    }

    private static void validateBrandName(String brandName) {
        if (brandName == null || brandName.isBlank()) {
            throw new InvalidBrandNameException();
        }
    }

    private static void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new InvalidProductNameException();
        }
    }

    private static void validateTemperatureType(TemperatureType temperatureType) {
        if (temperatureType == null) {
            throw new InvalidTemperatureTypeException();
        }
    }

    private static void validateRequiredObjects(Category category, ProductType productType, ProductInfo productInfo) {
        if (category == null) {
            throw new InvalidProductException("필수 연관 객체인 카테고리가 누락되었습니다.");
        }
        if (productType == null) {
            throw new InvalidProductException("필수 연관 객체인 상품 타입이 누락되었습니다.");
        }
        if (productInfo == null) {
            throw new InvalidProductException("필수 연관 객체인 상품 상세 정보가 누락되었습니다.");
        }
    }
}