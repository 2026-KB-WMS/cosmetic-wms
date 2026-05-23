package com.kb.cosmetic_wms.domain.product.fixture;

import com.kb.cosmetic_wms.domain.product.productInfo.ProductInfo;

public class ProductInfoTestBuilder {
    private String skinType = "건성";
    private String functionType = "보습";
    private int volume = 150;
    private String unit = "ml";
    private String ingredients = "정제수, 글리세린, 폴리솔베이트20, 다이소듐이디티에이";
    private String cautions =
            "1) 화장품 사용 시 또는 사용 후 직사광선에 의하여 사용부위가 붉은 반점, 부어오름 또는 가려움증 등의 이상 증상이나 부작용이 있는 경우에는 전문의 등과 상담할 것";
    private String storageCondition = "상온보관";

    public ProductInfoTestBuilder skinType(String skinType) {
        this.skinType = skinType;
        return this;
    }

    public ProductInfoTestBuilder functionType(String functionType) {
        this.functionType = functionType;
        return this;
    }

    public ProductInfoTestBuilder volume(int volume) {
        this.volume = volume;
        return this;
    }

    public ProductInfoTestBuilder unit(String unit) {
        this.unit = unit;
        return this;
    }

    public ProductInfoTestBuilder ingredients(String ingredients) {
        this.ingredients = ingredients;
        return this;
    }

    public ProductInfoTestBuilder cautions(String cautions) {
        this.cautions = cautions;
        return this;
    }

    public ProductInfoTestBuilder storageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
        return this;
    }

    public ProductInfo build() {
        return ProductInfo.create(
                skinType,
                functionType,
                volume,
                unit,
                ingredients,
                cautions,
                storageCondition
        );
    }
}
