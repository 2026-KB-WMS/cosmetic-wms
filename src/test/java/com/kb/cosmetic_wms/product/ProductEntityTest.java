package com.kb.cosmetic_wms.product;

import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.exception.*;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.fixture.ProductTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductEntityTest {

    @Test
    void 올바른_정보를_입력하면_상품_엔티티가_정상_생성된다() {
        Product product = new ProductTestBuilder().build();

        assertThat(product)
                .extracting(
                        Product::getBrandName,
                        Product::getProductName,
                        Product::getProductPrice,
                        Product::getTemperatureType
                )
                .containsExactly("BIO", "하이드라비오 토너", 15000, TemperatureType.ROOM);
    }

    @Test
    void 상품_생성_직후에는_SKU_코드가_null이다() {
        Product product = Product.create("BIO", "하이드라비오 토너", 15000,
                TemperatureType.ROOM,
                new ProductTestBuilder().build().getCategory(),
                new ProductTestBuilder().build().getProductType(),
                new ProductTestBuilder().build().getProductInfo());

        assertThat(product.getSkuCode()).isNull();
    }

    @Test
    void assignSkuCode를_호출하면_PK_기반_SKU_코드가_설정된다() {
        Product product = new ProductTestBuilder().build();
        product.assignSkuCode(1L);

        assertThat(product.getSkuCode()).isEqualTo("P000001");
    }

    @Test
    void PK가_6자리를_초과해도_SKU_코드가_올바르게_생성된다() {
        Product product = new ProductTestBuilder().build();
        product.assignSkuCode(1234567L);

        assertThat(product.getSkuCode()).isEqualTo("P1234567");
    }

    @Test
    void PK_1자리_숫자는_6자리_포맷으로_패딩된다() {
        Product product = new ProductTestBuilder().build();
        product.assignSkuCode(3L);

        assertThat(product.getSkuCode()).isEqualTo("P000003");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 브랜드명에_null이나_공백이_들어오면_예외를_던진다(String invalidBrandName) {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .brandName(invalidBrandName)
                        .build()
        ).isInstanceOf(InvalidBrandNameException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 상품명에_null이나_공백이_들어오면_예외를_던진다(String invalidProductName) {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productName(invalidProductName)
                        .build()
        ).isInstanceOf(InvalidProductNameException.class);
    }

    @Test
    void 상품_가격이_음수이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productPrice(-1)
                        .build()
        ).isInstanceOf(InvalidPriceException.class);
    }

    @Test
    void 상온_온도_타입으로_상품이_정상_생성된다() {
        Product product = new ProductTestBuilder()
                .temperatureType(TemperatureType.ROOM)
                .build();

        assertThat(product.getTemperatureType()).isEqualTo(TemperatureType.ROOM);
    }

    @Test
    void 보관_온도_타입이_null이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .temperatureType(null)
                        .build()
        ).isInstanceOf(InvalidTemperatureTypeException.class);
    }

    @Test
    void 상품_생성_시_카테고리가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .category(null)
                        .build()
        )
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("필수 연관 객체인 카테고리가 누락되었습니다.");
    }

    @Test
    void 상품_생성_시_상품타입이_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productType(null)
                        .build()
        )
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("필수 연관 객체인 상품 타입이 누락되었습니다.");
    }

    @Test
    void 상품_생성_시_상세정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productInfo(null)
                        .build()
        )
                .isInstanceOf(InvalidProductException.class)
                .hasMessage("필수 연관 객체인 상품 상세 정보가 누락되었습니다.");
    }
}