package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.category.Category;
import com.kb.cosmetic_wms.domain.product.category.CategoryConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CategoryEntityTest {

    @Test
    void 올바른_3자리_분류코드로_카테고리가_정상_생성된다() {
        // given
        String validCategoryCode = "SKN";
        String categoryName = "스킨케어";

        // when
        Category category = Category.create(validCategoryCode, categoryName);

        // then
        assertThat(category.getCategoryCode()).isEqualTo("SKN");
        assertThat(category.getCategoryName()).isEqualTo("스킨케어");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 카테고리_코드가_공백이면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CategoryConstants.CATEGORY_CODE_REQUIRED_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"S", "SKIN"})
    void 카테고리_코드가_3자리가_아니면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CategoryConstants.INVALID_CATEGORY_CODE_LENGTH_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"skn", "가나다", "ab1", "123", "^&%"})
    void 카테고리_코드가_영문_대문자_형식이_아니면_예외를_던진다(String invalidCategoryCode) {
        assertThatThrownBy(() -> Category.create(invalidCategoryCode, "스킨케어"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CategoryConstants.INVALID_CATEGORY_CODE_FORMAT_MESSAGE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void 카테고리_이름이_공백이면_예외를_던진다(String invalidCategoryName) {
        assertThatThrownBy(() -> Category.create("SKN", invalidCategoryName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(CategoryConstants.CATEGORY_NAME_REQUIRED_MESSAGE);
    }
}
