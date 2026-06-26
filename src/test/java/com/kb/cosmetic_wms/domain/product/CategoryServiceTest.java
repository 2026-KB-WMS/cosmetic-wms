package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.category.application.port.in.CategoryResult;
import com.kb.cosmetic_wms.product.category.application.port.in.RegisterCategoryCommand;
import com.kb.cosmetic_wms.product.category.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.category.application.service.CategoryService;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryErrorCode;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryInUseException;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.product.category.domain.exception.DuplicateCategoryException;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryPort categoryPort;

    @Mock
    private ProductPort productPort;

    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        defaultCategory = Category.create("SKN", "스킨케어");
        ReflectionTestUtils.setField(defaultCategory, "categoryId", 1L);
    }

    @Nested
    class 카테고리_목록_조회 {

        @Test
        void 카테고리가_여러_개_존재하면_전체_목록을_반환한다() {
            // given
            Category secondCategory = Category.create("MKP", "메이크업");
            ReflectionTestUtils.setField(secondCategory, "categoryId", 2L);

            given(categoryPort.findAll()).willReturn(List.of(defaultCategory, secondCategory));

            // when
            List<CategoryResult> result = categoryService.findAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).categoryId()).isEqualTo(1L);
            assertThat(result.get(0).categoryCode()).isEqualTo("SKN");
            assertThat(result.get(0).categoryName()).isEqualTo("스킨케어");
            assertThat(result.get(1).categoryId()).isEqualTo(2L);
            assertThat(result.get(1).categoryCode()).isEqualTo("MKP");
        }

        @Test
        void 등록된_카테고리가_없으면_빈_목록을_반환한다() {
            // given
            given(categoryPort.findAll()).willReturn(List.of());

            // when
            List<CategoryResult> result = categoryService.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 카테고리_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_카테고리_정보를_반환한다() {
            // given
            given(categoryPort.findById(1L)).willReturn(Optional.of(defaultCategory));

            // when
            CategoryResult result = categoryService.findById(1L);

            // then
            assertThat(result.categoryId()).isEqualTo(1L);
            assertThat(result.categoryCode()).isEqualTo("SKN");
            assertThat(result.categoryName()).isEqualTo("스킨케어");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_CategoryNotFoundException이_발생한다() {
            // given
            given(categoryPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.findById(999L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 카테고리_등록 {

        @Test
        void 올바른_카테고리_정보를_입력하면_정상적으로_등록된다() {
            // given
            RegisterCategoryCommand command = new RegisterCategoryCommand("SKN", "스킨케어");

            given(categoryPort.existsByCode("SKN")).willReturn(false);
            given(categoryPort.save(any(Category.class))).willReturn(defaultCategory);

            // when
            CategoryResult result = categoryService.register(command);

            // then
            assertThat(result.categoryId()).isEqualTo(1L);
            assertThat(result.categoryCode()).isEqualTo("SKN");
            assertThat(result.categoryName()).isEqualTo("스킨케어");
        }

        @Test
        void 등록된_카테고리의_코드와_이름이_입력값과_일치한다() {
            // given
            RegisterCategoryCommand command = new RegisterCategoryCommand("MKP", "메이크업");
            Category savedCategory = Category.create("MKP", "메이크업");
            ReflectionTestUtils.setField(savedCategory, "categoryId", 2L);

            given(categoryPort.existsByCode("MKP")).willReturn(false);
            given(categoryPort.save(any(Category.class))).willReturn(savedCategory);

            // when
            CategoryResult result = categoryService.register(command);

            // then
            assertThat(result.categoryCode()).isEqualTo("MKP");
            assertThat(result.categoryName()).isEqualTo("메이크업");
        }

        @Test
        void 중복된_카테고리_코드로_등록을_시도하면_DuplicateCategoryException이_발생한다() {
            // given
            RegisterCategoryCommand command = new RegisterCategoryCommand("SKN", "스킨케어 중복");
            given(categoryPort.existsByCode("SKN")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> categoryService.register(command))
                    .isInstanceOf(DuplicateCategoryException.class)
                    .hasMessage(CategoryErrorCode.DUPLICATE_CATEGORY.getMessage());
            verify(categoryPort, never()).save(any());
        }
    }

    @Nested
    class 카테고리_삭제 {

        @Test
        void 참조_상품이_없는_카테고리를_삭제하면_deleteById가_호출된다() {
            // given
            given(categoryPort.findById(1L)).willReturn(Optional.of(defaultCategory));
            given(productPort.existsByCategoryId(1L)).willReturn(false);

            // when
            categoryService.delete(1L);

            // then
            verify(categoryPort).deleteById(1L);
        }

        @Test
        void 존재하지_않는_ID로_삭제를_시도하면_CategoryNotFoundException이_발생한다() {
            // given
            given(categoryPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.delete(999L))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage());
            verify(categoryPort, never()).deleteById(any());
        }

        @Test
        void 참조_상품이_존재하는_카테고리를_삭제하면_CategoryInUseException이_발생한다() {
            // given
            given(categoryPort.findById(1L)).willReturn(Optional.of(defaultCategory));
            given(productPort.existsByCategoryId(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> categoryService.delete(1L))
                    .isInstanceOf(CategoryInUseException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_IN_USE.getMessage());
            verify(categoryPort, never()).deleteById(any());
        }
    }
}