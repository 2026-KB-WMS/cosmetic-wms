package com.kb.cosmetic_wms.product;

import com.kb.cosmetic_wms.product.category.domain.exception.InvalidCategoryCodeException;
import com.kb.cosmetic_wms.product.category.domain.exception.InvalidCategoryNameException;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryEntityTest {

    @Test
    void 올바른_3자리_분류코드로_카테고리가_정상_생성된다() {
        Category category = Category.create("SKN", "스킨케어");

        assertThat(category.getCategoryCode()).isEqualTo("SKN");
        assertThat(category.getCategoryName()).isEqualTo("스킨케어");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 카테고리_코드가_공백이면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(InvalidCategoryCodeException.class)
                .hasMessage("카테고리 코드는 필수 입력 항목입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"S", "SKIN"})
    void 카테고리_코드가_3자리가_아니면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(InvalidCategoryCodeException.class)
                .hasMessage("카테고리 코드는 3자리여야 합니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"skn", "가나다", "ab1", "123", "^&%"})
    void 카테고리_코드가_영문_대문자_형식이_아니면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(InvalidCategoryCodeException.class)
                .hasMessage("카테고리 코드는 영문 대문자만 가능합니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 카테고리_이름이_공백이면_예외를_던진다(String invalidCategoryName) {
        assertThatThrownBy(() -> Category.create("SKN", invalidCategoryName))
                .isInstanceOf(InvalidCategoryNameException.class);
    }
}