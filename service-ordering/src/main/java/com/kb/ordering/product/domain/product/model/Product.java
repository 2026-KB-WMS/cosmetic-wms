package com.kb.ordering.product.domain.product.model;

import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.exception.InvalidBrandNameException;
import com.kb.ordering.product.domain.product.exception.InvalidProductException;
import com.kb.ordering.product.domain.product.exception.InvalidProductNameException;
import com.kb.ordering.product.domain.product.exception.InvalidTemperatureTypeException;
import com.kb.ordering.product.domain.product.valueobject.Price;
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
    private final Long categoryId;
    private final Long productTypeId;
    private ProductInfo productInfo;

    private Product(Long productId, String skuCode, String brandName, String productName,
                    Price price, TemperatureType temperatureType,
                    Long categoryId, Long productTypeId, ProductInfo productInfo) {
        this.productId = productId;
        this.skuCode = skuCode;
        this.brandName = brandName;
        this.productName = productName;
        this.price = price;
        this.temperatureType = temperatureType;
        this.categoryId = categoryId;
        this.productTypeId = productTypeId;
        this.productInfo = productInfo;
    }

    public static Product create(String brandName, String productName, int productPrice,
                                 TemperatureType temperatureType, Long categoryId,
                                 Long productTypeId, ProductInfo productInfo) {
        validateBrandName(brandName);
        validateProductName(productName);
        Price price = Price.of(productPrice);
        validateTemperatureType(temperatureType);
        validateAssociations(categoryId, productTypeId, productInfo);

        return new Product(null, null, brandName, productName, price,
                temperatureType, categoryId, productTypeId, productInfo);
    }

    public static Product reconstitute(Long productId, String skuCode, String brandName, String productName,
                                       int productPrice, TemperatureType temperatureType,
                                       Long categoryId, Long productTypeId, ProductInfo productInfo) {
        return new Product(productId, skuCode, brandName, productName, Price.of(productPrice),
                temperatureType, categoryId, productTypeId, productInfo);
    }

    public void assignSkuCode(Long productId) {
        this.skuCode = String.format(SKU_FORMAT, productId);
    }

    public void update(String productName, int productPrice,
                       TemperatureType temperatureType, ProductInfo productInfo) {
        validateProductName(productName);
        Price newPrice = Price.of(productPrice);
        validateTemperatureType(temperatureType);
        if (productInfo == null) {
            throw new InvalidProductException("필수 연관 객체인 상품 상세 정보가 누락되었습니다.");
        }

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

    private static void validateAssociations(Long categoryId, Long productTypeId, ProductInfo productInfo) {
        if (categoryId == null) {
            throw new InvalidProductException("필수 연관 객체인 카테고리가 누락되었습니다.");
        }
        if (productTypeId == null) {
            throw new InvalidProductException("필수 연관 객체인 상품 타입이 누락되었습니다.");
        }
        if (productInfo == null) {
            throw new InvalidProductException("필수 연관 객체인 상품 상세 정보가 누락되었습니다.");
        }
    }
}