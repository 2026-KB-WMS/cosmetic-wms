package com.kb.ordering.product.fixture;

import com.kb.ordering.product.domain.product.enums.TemperatureType;
import com.kb.ordering.product.domain.product.model.Product;

public class ProductTestBuilder {
    private String brandName = "BIO";
    private String productName = "하이드라비오 토너";
    private int productPrice = 15000;
    private TemperatureType temperatureType = TemperatureType.ROOM;
    private long skuId = 1L;
    private Long categoryId = 1L;
    private Long productTypeId = 1L;
    private com.kb.ordering.product.domain.product.model.ProductInfo productInfo =
            new ProductInfoTestBuilder().volume(150).unit("ml").build();

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

    public ProductTestBuilder categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ProductTestBuilder productTypeId(Long productTypeId) {
        this.productTypeId = productTypeId;
        return this;
    }

    public ProductTestBuilder productInfo(com.kb.ordering.product.domain.product.model.ProductInfo productInfo) {
        this.productInfo = productInfo;
        return this;
    }

    public Product build() {
        Product product = Product.create(
                brandName, productName, productPrice, temperatureType,
                categoryId, productTypeId, productInfo
        );
        product.assignSkuCode(skuId);
        return product;
    }
}
