package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.constants.ProductConstants;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductEntityTest {

    @Test
    void 올바른_정보를_입력하면_상품_엔티티가_정상_생성된다() {
        // given & when
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
    void 상품_생성_시_올바른_SKU_코드가_정상_생성된다() {
        // given & when
        Product product = new ProductTestBuilder().build();

        // then
        assertThat(product.getSkuCode()).isEqualTo("BIO-SKN-TON-150-0001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 브랜드명에_null이나_공백이_들어오면_예외를_던진다(String invalidBrandName) {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .brandName(invalidBrandName)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.BRAND_NAME_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"bio", "Bio", "  bIo  "})
    void 브랜드명에_공백이나_소문자가_있어도_SKU_코드는_공백이_제거된_대문자_포맷으로_조립된다(String mixedBrandName) {
        // given & when
        Product product = new ProductTestBuilder()
                .brandName(mixedBrandName)
                .build();

        assertThat(product.getSkuCode()).isEqualTo("BIO-SKN-TON-150-0001");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 상품명에_null이나_공백이_들어오면_예외를_던진다(String invalidProductName) {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productName(invalidProductName)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.PRODUCT_NAME_REQUIRED_MESSAGE);
    }

    @Test
    void 상품_가격이_음수이면_예외를_던진다() {
        // given
        int negativePrice = -1;

        // when & then
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productPrice(negativePrice)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.INVALID_PRODUCT_PRICE_MESSAGE);
    }

    @Test
    void 상온_온도_타입으로_상품이_정상_생성된다() {
        // given & when
        Product product = new ProductTestBuilder()
                .temperatureType(TemperatureType.ROOM)
                .build();

        // then
        assertThat(product.getTemperatureType()).isEqualTo(TemperatureType.ROOM);
    }

    @Test
    void 보관_온도_타입이_null이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .temperatureType(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.TEMPERATURE_TYPE_REQUIRED_MESSAGE);
    }

    @Test
    void 한_자릿수_일련번호가_입력되면_SKU_코드에서_네자리_포맷으로_변환되어_조립된다() {
        int sequence = 3;

        Product product = new ProductTestBuilder()
                .sequence(sequence)
                .build();

        assertThat(product.getSkuCode()).isEqualTo("BIO-SKN-TON-150-0003");
    }

    @Test
    void 세_자릿수_일련번호가_입력되면_SKU_코드에서_네자리_포맷으로_변환되어_조립된다() {
        int sequence = 777;

        Product product = new ProductTestBuilder()
                .sequence(sequence)
                .build();

        assertThat(product.getSkuCode()).isEqualTo("BIO-SKN-TON-150-0777");
    }

    @Test
    void SKU_일련번호가_4자리를_초과하면_예외를_던진다() {
        int invalidSequence = 10000;

        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .sequence(invalidSequence)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.INVALID_SEQUENCE_MAX_MESSAGE);
    }

    @Test
    void 상품_생성_시_카테고리가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .category(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.CATEGORY_REQUIRED_MESSAGE);
    }

    @Test
    void 상품_생성_시_상품타입이_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productType(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.PRODUCT_TYPE_REQUIRED_MESSAGE);
    }

    @Test
    void 상품_생성_시_상세정보가_누락되면_예외를_던진다() {
        assertThatThrownBy(() ->
                new ProductTestBuilder()
                        .productInfo(null)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductConstants.PRODUCT_INFO_REQUIRED_MESSAGE);
    }
}
