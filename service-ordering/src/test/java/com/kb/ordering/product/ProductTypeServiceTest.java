package com.kb.ordering.product;

import com.kb.ordering.product.application.port.in.dto.ProductTypeResult;
import com.kb.ordering.product.application.port.in.dto.RegisterProductTypeCommand;
import com.kb.ordering.product.application.port.out.ProductPort;
import com.kb.ordering.product.application.port.out.ProductTypePort;
import com.kb.ordering.product.application.service.ProductTypeService;
import com.kb.ordering.product.domain.producttype.exception.DuplicateProductTypeException;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeErrorCode;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeInUseException;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeNotFoundException;
import com.kb.ordering.product.domain.producttype.model.ProductType;
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
public class ProductTypeServiceTest {

    @InjectMocks
    private ProductTypeService productTypeService;

    @Mock
    private ProductTypePort productTypePort;

    @Mock
    private ProductPort productPort;

    private ProductType defaultProductType;

    @BeforeEach
    void setUp() {
        defaultProductType = ProductType.create("TON", "토너");
        ReflectionTestUtils.setField(defaultProductType, "productTypeId", 1L);
    }

    @Nested
    class 상품_타입_목록_조회 {

        @Test
        void 상품_타입이_여러_개_존재하면_전체_목록을_반환한다() {
            // given
            ProductType secondType = ProductType.create("SRM", "세럼");
            ReflectionTestUtils.setField(secondType, "productTypeId", 2L);

            given(productTypePort.findAll()).willReturn(List.of(defaultProductType, secondType));

            // when
            List<ProductTypeResult> result = productTypeService.findAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productTypeId()).isEqualTo(1L);
            assertThat(result.get(0).typeCode()).isEqualTo("TON");
            assertThat(result.get(0).typeName()).isEqualTo("토너");
            assertThat(result.get(1).productTypeId()).isEqualTo(2L);
            assertThat(result.get(1).typeCode()).isEqualTo("SRM");
        }

        @Test
        void 등록된_상품_타입이_없으면_빈_목록을_반환한다() {
            // given
            given(productTypePort.findAll()).willReturn(List.of());

            // when
            List<ProductTypeResult> result = productTypeService.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 상품_타입_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_상품_타입_정보를_반환한다() {
            // given
            given(productTypePort.findById(1L)).willReturn(Optional.of(defaultProductType));

            // when
            ProductTypeResult result = productTypeService.findById(1L);

            // then
            assertThat(result.productTypeId()).isEqualTo(1L);
            assertThat(result.typeCode()).isEqualTo("TON");
            assertThat(result.typeName()).isEqualTo("토너");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_ProductTypeNotFoundException이_발생한다() {
            // given
            given(productTypePort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productTypeService.findById(999L))
                    .isInstanceOf(ProductTypeNotFoundException.class)
                    .hasMessage(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품_타입_등록 {

        @Test
        void 올바른_상품_타입_정보를_입력하면_정상적으로_등록된다() {
            // given
            RegisterProductTypeCommand command = new RegisterProductTypeCommand("TON", "토너");

            given(productTypePort.existsByCode("TON")).willReturn(false);
            given(productTypePort.save(any(ProductType.class))).willReturn(defaultProductType);

            // when
            ProductTypeResult result = productTypeService.register(command);

            // then
            assertThat(result.productTypeId()).isEqualTo(1L);
            assertThat(result.typeCode()).isEqualTo("TON");
            assertThat(result.typeName()).isEqualTo("토너");
        }

        @Test
        void 등록된_상품_타입의_코드와_이름이_입력값과_일치한다() {
            // given
            RegisterProductTypeCommand command = new RegisterProductTypeCommand("SRM", "세럼");
            ProductType savedType = ProductType.create("SRM", "세럼");
            ReflectionTestUtils.setField(savedType, "productTypeId", 2L);

            given(productTypePort.existsByCode("SRM")).willReturn(false);
            given(productTypePort.save(any(ProductType.class))).willReturn(savedType);

            // when
            ProductTypeResult result = productTypeService.register(command);

            // then
            assertThat(result.typeCode()).isEqualTo("SRM");
            assertThat(result.typeName()).isEqualTo("세럼");
        }

        @Test
        void 중복된_타입_코드로_등록을_시도하면_DuplicateProductTypeException이_발생한다() {
            // given
            RegisterProductTypeCommand command = new RegisterProductTypeCommand("TON", "토너 중복");
            given(productTypePort.existsByCode("TON")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> productTypeService.register(command))
                    .isInstanceOf(DuplicateProductTypeException.class)
                    .hasMessage(ProductTypeErrorCode.DUPLICATE_PRODUCT_TYPE.getMessage());
            verify(productTypePort, never()).save(any());
        }
    }

    @Nested
    class 상품_타입_삭제 {

        @Test
        void 참조_상품이_없는_상품_타입을_삭제하면_deleteById가_호출된다() {
            // given
            given(productTypePort.findById(1L)).willReturn(Optional.of(defaultProductType));
            given(productPort.existsByProductTypeId(1L)).willReturn(false);

            // when
            productTypeService.delete(1L);

            // then
            verify(productTypePort).deleteById(1L);
        }

        @Test
        void 존재하지_않는_ID로_삭제를_시도하면_ProductTypeNotFoundException이_발생한다() {
            // given
            given(productTypePort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productTypeService.delete(999L))
                    .isInstanceOf(ProductTypeNotFoundException.class)
                    .hasMessage(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND.getMessage());
            verify(productTypePort, never()).deleteById(any());
        }

        @Test
        void 참조_상품이_존재하는_상품_타입을_삭제하면_ProductTypeInUseException이_발생한다() {
            // given
            given(productTypePort.findById(1L)).willReturn(Optional.of(defaultProductType));
            given(productPort.existsByProductTypeId(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> productTypeService.delete(1L))
                    .isInstanceOf(ProductTypeInUseException.class)
                    .hasMessage(ProductTypeErrorCode.PRODUCT_TYPE_IN_USE.getMessage());
            verify(productTypePort, never()).deleteById(any());
        }
    }
}