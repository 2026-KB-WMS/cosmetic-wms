package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.entity.ProductInfo;
import com.kb.cosmetic_wms.domain.product.fixture.ProductInfoTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductInfoEntityTest {

    @Test
    void 유효한_값이면_ProductInfo가_정상_생성되며_내부_Volume_객체도_정상_생성된다() {
        // given & when
        ProductInfo productInfo = new ProductInfoTestBuilder().build();

        // then
        assertThat(productInfo)
                .extracting(
                        ProductInfo::getSkinType,
                        ProductInfo::getFunctionType,
                        ProductInfo::getStorageCondition
                )
                .containsExactly("건성", "보습", "상온보관");

        assertThat(productInfo.getVolume()).isNotNull();
        assertThat(productInfo.getVolume().value()).isEqualTo(150);
        assertThat(productInfo.getVolume().unit()).isEqualTo("ml");
    }

    @Test
    void skinType이_50자를_초과하면_예외를_던진다() {
        String invalidSkinType = "가".repeat(51);

        assertThatThrownBy(() ->
                new ProductInfoTestBuilder()
                        .skinType(invalidSkinType)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductInfoConstants.INVALID_SKIN_TYPE_LENGTH_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void skinType에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String skinType) {
        // given & when
        ProductInfo info = new ProductInfoTestBuilder().skinType(skinType).build();

        // then
        assertThat(info.getSkinType()).isEqualTo("");
    }

    @Test
    void functionType이_100자를_초과하면_예외를_던진다() {
        String invalidFunctionType = "나".repeat(101);

        assertThatThrownBy(() ->
                new ProductInfoTestBuilder()
                        .functionType(invalidFunctionType)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductInfoConstants.INVALID_FUNCTION_TYPE_LENGTH_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void functionType에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String functionType) {
        // given & when
        ProductInfo info = new ProductInfoTestBuilder().functionType(functionType).build();

        // then
        assertThat(info.getFunctionType()).isEqualTo("");
    }

    @Test
    void storageCondition이_255자를_초과하면_예외를_던진다() {
        String invalidStorageCondition = "다".repeat(256);

        assertThatThrownBy(() ->
                new ProductInfoTestBuilder()
                        .storageCondition(invalidStorageCondition)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductInfoConstants.INVALID_STORAGE_CONDITION_LENGTH_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void storageCondition에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String storageCondition) {
        // given & when
        ProductInfo info = new ProductInfoTestBuilder().storageCondition(storageCondition).build();

        // then
        assertThat(info.getStorageCondition()).isEqualTo("");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void ingredients에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String ingredients) {
        // given & when
        ProductInfo info = new ProductInfoTestBuilder().ingredients(ingredients).build();

        // then
        assertThat(info.getIngredients()).isEqualTo("");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void cautions에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String cautions) {
        // given & when
        ProductInfo info = new ProductInfoTestBuilder().cautions(cautions).build();

        // then
        assertThat(info.getCautions()).isEqualTo("");
    }
}
