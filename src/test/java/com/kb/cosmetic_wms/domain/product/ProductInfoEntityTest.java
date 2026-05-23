package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.fixture.ProductInfoTestBuilder;
import com.kb.cosmetic_wms.domain.product.productInfo.ProductInfo;
import com.kb.cosmetic_wms.domain.product.productInfo.ProductInfoConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductInfoEntityTest {

    @Test
    void 유효한_값이면_ProductInfo가_생성된다() {
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
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -50})
    void 화장품_용량이_0이하이면_예외를_던진다(int invalidVolume) {
        assertThatThrownBy(() ->
                new ProductInfoTestBuilder()
                        .volume(invalidVolume)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductInfoConstants.INVALID_VOLUME_MIN_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(ints = {10001, 50000})
    void 화장품_용량이_최대_제한을_초과하면_예외를_던진다(int exceedVolume) {
        assertThatThrownBy(() ->
                new ProductInfoTestBuilder()
                        .volume(exceedVolume)
                        .build()
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ProductInfoConstants.INVALID_VOLUME_MAX_MESSAGE);
    }
}
