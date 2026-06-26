package com.kb.cosmetic_wms.product;

import com.kb.cosmetic_wms.product.product.domain.exception.InvalidProductInfoException;
import com.kb.cosmetic_wms.product.product.domain.model.ProductInfo;
import com.kb.cosmetic_wms.product.fixture.ProductInfoTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductInfoEntityTest {

    @Test
    void 유효한_값이면_ProductInfo가_정상_생성되며_내부_Volume_객체도_정상_생성된다() {
        ProductInfo productInfo = new ProductInfoTestBuilder().build();

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
                .isInstanceOf(InvalidProductInfoException.class)
                .hasMessage("피부 타입 정보는 " + ProductInfo.SKIN_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void skinType에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String skinType) {
        ProductInfo info = new ProductInfoTestBuilder().skinType(skinType).build();

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
                .isInstanceOf(InvalidProductInfoException.class)
                .hasMessage("기능성 타입 정보는 " + ProductInfo.FUNCTION_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void functionType에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String functionType) {
        ProductInfo info = new ProductInfoTestBuilder().functionType(functionType).build();

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
                .isInstanceOf(InvalidProductInfoException.class)
                .hasMessage("보관 조건 정보는 " + ProductInfo.STORAGE_CONDITION_MAX_LENGTH + "자를 초과할 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void storageCondition에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String storageCondition) {
        ProductInfo info = new ProductInfoTestBuilder().storageCondition(storageCondition).build();

        assertThat(info.getStorageCondition()).isEqualTo("");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void ingredients에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String ingredients) {
        ProductInfo info = new ProductInfoTestBuilder().ingredients(ingredients).build();

        assertThat(info.getIngredients()).isEqualTo("");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void cautions에_null이나_공백이_들어오면_빈_문자열로_정상_생성된다(String cautions) {
        ProductInfo info = new ProductInfoTestBuilder().cautions(cautions).build();

        assertThat(info.getCautions()).isEqualTo("");
    }
}
