package com.kb.cosmetic_wms.product.fixture;

import com.kb.cosmetic_wms.product.product.adapter.in.web.RegisterProductRequest;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;

public class ProductDtoBuilder {

    private String brandName = "BIO";
    private String productName = "하이드라비오 토너";
    private int productPrice = 15000;
    private TemperatureType temperatureType = TemperatureType.ROOM;
    private Long categoryId = 1L;
    private Long productTypeId = 1L;
    private String skinType = "건성";
    private String functionType = "보습";
    private int volume = 150;
    private String unit = "ml";
    private String ingredients = "정제수, 글리세린, 폴리솔베이트20, 다이소듐이디티에이";
    private String cautions =
            "1) 화장품 사용 시 또는 사용 후 직사광선에 의하여 사용부위가 붉은 반점, 부어오름 또는 가려움증 등의 이상 증상이나 부작용이 있는 경우에는 전문의 등과 상담할 것";
    private String storageCondition = "상온보관";

    public ProductDtoBuilder brandName(String brandName) {
        this.brandName = brandName;
        return this;
    }

    public ProductDtoBuilder productName(String productName) {
        this.productName = productName;
        return this;
    }

    public ProductDtoBuilder productPrice(int productPrice) {
        this.productPrice = productPrice;
        return this;
    }

    public ProductDtoBuilder temperatureType(TemperatureType temperatureType) {
        this.temperatureType = temperatureType;
        return this;
    }

    public ProductDtoBuilder categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ProductDtoBuilder productTypeId(Long productTypeId) {
        this.productTypeId = productTypeId;
        return this;
    }

    public ProductDtoBuilder volume(int volume) {
        this.volume = volume;
        return this;
    }

    public ProductDtoBuilder unit(String unit) {
        this.unit = unit;
        return this;
    }

    public ProductDtoBuilder skinType(String skinType) {
        this.skinType = skinType;
        return this;
    }

    public ProductDtoBuilder functionType(String functionType) {
        this.functionType = functionType;
        return this;
    }

    public ProductDtoBuilder ingredients(String ingredients) {
        this.ingredients = ingredients;
        return this;
    }

    public ProductDtoBuilder cautions(String cautions) {
        this.cautions = cautions;
        return this;
    }

    public ProductDtoBuilder storageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
        return this;
    }

    public RegisterProductRequest build() {
        return new RegisterProductRequest(
                brandName,
                productName,
                productPrice,
                temperatureType,
                categoryId,
                productTypeId,
                new RegisterProductRequest.ProductInfoRequest(
                        skinType,
                        functionType,
                        new RegisterProductRequest.VolumeRequest(volume, unit),
                        ingredients,
                        cautions,
                        storageCondition
                )
        );
    }
}