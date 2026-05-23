package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.entity.ProductInfo;
import com.kb.cosmetic_wms.domain.product.fixture.ProductInfoTestBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
}
