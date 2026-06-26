package com.kb.cosmetic_wms.domain.product.fixture;

import com.kb.cosmetic_wms.product.category.domain.model.Category;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.product.domain.model.ProductInfo;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;

public class ProductTestBuilder {
    private String brandName = "BIO";
    private String productName = "하이드라비오 토너";
    private int productPrice = 15000;
    private TemperatureType temperatureType = TemperatureType.ROOM;
    private long skuId = 1L;

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

    public ProductTestBuilder skuId(long skuId) {
        this.skuId = skuId;
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
        Product product = Product.create(
                brandName, productName, productPrice, temperatureType,
                category, productType, productInfo
        );
        product.assignSkuCode(skuId);
        return product;
    }
}
