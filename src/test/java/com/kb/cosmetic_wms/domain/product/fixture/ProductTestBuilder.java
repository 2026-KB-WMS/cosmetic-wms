package com.kb.cosmetic_wms.domain.product.fixture;

import com.kb.cosmetic_wms.domain.product.entity.Category;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.entity.ProductInfo;
import com.kb.cosmetic_wms.domain.product.entity.ProductType;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;

public class ProductTestBuilder {
    private String brandName = "BIO";
    private String productName = "하이드라비오 토너";
    private int productPrice = 15000;
    private TemperatureType temperatureType = TemperatureType.ROOM;
    private int sequence = 1;

    private Category category = Category.create("SKN", "스킨케어");
    private ProductType productType = ProductType.create("TON", "토너");
    private ProductInfo productInfo = new ProductInfoTestBuilder().volume(150).unit("ml").build();

    public ProductTestBuilder brandName(String brandName) {
        this.brandName = brandName;
        return this;
    }

    public ProductTestBuilder productName(String productName) {
        this.productName = productName;
        return this;
    }

    public ProductTestBuilder productPrice(int productPrice) {
        this.productPrice = productPrice;
        return this;
    }

    public ProductTestBuilder temperatureType(TemperatureType temperatureType) {
        this.temperatureType = temperatureType;
        return this;
    }

    public ProductTestBuilder sequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public ProductTestBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public ProductTestBuilder productType(ProductType productType) {
        this.productType = productType;
        return this;
    }

    public ProductTestBuilder productInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
        return this;
    }

    public Product build() {
        return Product.create(
                brandName,
                productName,
                productPrice,
                temperatureType,
                category,
                productType,
                productInfo,
                sequence
        );
    }
}
