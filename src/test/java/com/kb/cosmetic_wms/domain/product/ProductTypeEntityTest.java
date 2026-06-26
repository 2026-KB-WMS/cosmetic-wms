package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.product.producttype.domain.exception.InvalidTypeCodeException;
import com.kb.cosmetic_wms.product.producttype.domain.exception.InvalidTypeNameException;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductTypeEntityTest {

    @Test
    void 올바른_3자리_타입코드로_상품타입_객체가_정상_생성된다() {
        ProductType productType = ProductType.create("TON", "토너");

        assertThat(productType.getTypeCode()).isEqualTo("TON");
        assertThat(productType.getTypeName()).isEqualTo("토너");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 타입코드가_공백이면_예외를_던진다(String invalidTypeCode) {
        assertThatThrownBy(() -> ProductType.create(invalidTypeCode, "토너"))
                .isInstanceOf(InvalidTypeCodeException.class)
                .hasMessage("타입 코드는 필수 입력 항목입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"T", "TONER"})
    void 타입코드가_3자리가_아니면_예외를_던진다(String invalidTypeCode) {
        assertThatThrownBy(() -> ProductType.create(invalidTypeCode, "토너"))
                .isInstanceOf(InvalidTypeCodeException.class)
                .hasMessage("타입 코드는 3자리여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "가나다", "a@c", "^_%"})
    void 타입코드가_영문_대문자_형식이_아니면_예외를_던진다(String invalidTypeCode) {
        assertThatThrownBy(() -> ProductType.create(invalidTypeCode, "토너"))
                .isInstanceOf(InvalidTypeCodeException.class)
                .hasMessage("타입 코드는 영문 대문자만 가능합니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 타입이름이_공백이면_예외를_던진다(String invalidTypeName) {
        assertThatThrownBy(() -> ProductType.create("TON", invalidTypeName))
                .isInstanceOf(InvalidTypeNameException.class);
    }
}