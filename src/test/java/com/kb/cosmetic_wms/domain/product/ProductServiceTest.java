package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.product.product.application.port.in.ProductResult;
import com.kb.cosmetic_wms.product.product.application.port.in.ProductSummaryResult;
import com.kb.cosmetic_wms.product.product.application.port.in.RegisterProductCommand;
import com.kb.cosmetic_wms.product.product.application.port.in.UpdateProductCommand;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.product.application.service.ProductService;
import com.kb.cosmetic_wms.product.category.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import com.kb.cosmetic_wms.product.product.domain.enums.TemperatureType;
import com.kb.cosmetic_wms.product.product.domain.exception.DuplicateProductException;
import com.kb.cosmetic_wms.product.product.domain.exception.ProductErrorCode;
import com.kb.cosmetic_wms.product.product.domain.exception.ProductNotFoundException;
import com.kb.cosmetic_wms.product.product.domain.model.Product;
import com.kb.cosmetic_wms.product.producttype.application.port.out.ProductTypePort;
import com.kb.cosmetic_wms.product.producttype.domain.exception.ProductTypeNotFoundException;
import com.kb.cosmetic_wms.product.producttype.domain.model.ProductType;
import com.kb.cosmetic_wms.domain.product.fixture.ProductDtoBuilder;
import com.kb.cosmetic_wms.domain.product.fixture.ProductInfoTestBuilder;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductPort productPort;

    @Mock
    private CategoryPort categoryPort;

    @Mock
    private ProductTypePort productTypePort;

    private Category defaultCategory;
    private ProductType defaultProductType;
    private Product defaultProduct;

    @BeforeEach
    void setUp() {
        defaultCategory = Category.create("SKN", "스킨케어");
        ReflectionTestUtils.setField(defaultCategory, "categoryId", 1L);

        defaultProductType = ProductType.create("TON", "토너");
        ReflectionTestUtils.setField(defaultProductType, "productTypeId", 1L);

        defaultProduct = new ProductTestBuilder()
                .category(defaultCategory)
                .productType(defaultProductType)
                .build();
        ReflectionTestUtils.setField(defaultProduct, "productId", 1L);
    }

    @Nested
    class 상품_목록_조회 {

        @Test
        void 상품이_여러_개_존재하면_전체_목록을_반환한다() {
            // given
            Product secondProduct = new ProductTestBuilder()
                    .category(defaultCategory)
                    .productType(defaultProductType)
                    .brandName("LABO")
                    .productName("라보 에센스")
                    .skuId(2L)
                    .build();
            ReflectionTestUtils.setField(secondProduct, "productId", 2L);

            given(productPort.findAll()).willReturn(List.of(defaultProduct, secondProduct));

            // when
            List<ProductSummaryResult> result = productService.findAll();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(0).skuCode()).isEqualTo("P000001");
            assertThat(result.get(1).productId()).isEqualTo(2L);
            assertThat(result.get(1).brandName()).isEqualTo("LABO");
        }

        @Test
        void 등록된_상품이_없으면_빈_목록을_반환한다() {
            // given
            given(productPort.findAll()).willReturn(List.of());

            // when
            List<ProductSummaryResult> result = productService.findAll();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 목록_응답에는_핵심_필드만_반환된다() {
            // given
            given(productPort.findAll()).willReturn(List.of(defaultProduct));

            // when
            List<ProductSummaryResult> result = productService.findAll();

            // then
            ProductSummaryResult summary = result.get(0);
            assertThat(summary.productId()).isEqualTo(1L);
            assertThat(summary.skuCode()).isEqualTo("P000001");
            assertThat(summary.brandName()).isEqualTo("BIO");
            assertThat(summary.productName()).isEqualTo("하이드라비오 토너");
            assertThat(summary.productPrice()).isEqualTo(15000);
        }
    }

    @Nested
    class 상품_상세_조회 {

        @Test
        void 존재하는_ID로_조회하면_상품_상세_정보를_반환한다() {
            // given
            given(productPort.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            ProductResult result = productService.findById(1L);

            // then
            assertThat(result.productId()).isEqualTo(1L);
            assertThat(result.skuCode()).isEqualTo("P000001");
            assertThat(result.brandName()).isEqualTo("BIO");
            assertThat(result.productName()).isEqualTo("하이드라비오 토너");
            assertThat(result.productPrice()).isEqualTo(15000);
        }

        @Test
        void 상세_응답에는_productInfo가_포함된다() {
            // given
            Product product = new ProductTestBuilder()
                    .category(defaultCategory)
                    .productType(defaultProductType)
                    .productInfo(new ProductInfoTestBuilder()
                            .skinType("지성")
                            .functionType("모공 케어")
                            .volume(200).unit("ml")
                            .storageCondition("서늘한 곳 보관")
                            .build())
                    .build();
            ReflectionTestUtils.setField(product, "productId", 1L);

            given(productPort.findById(1L)).willReturn(Optional.of(product));

            // when
            ProductResult result = productService.findById(1L);

            // then
            assertThat(result.skinType()).isEqualTo("지성");
            assertThat(result.functionType()).isEqualTo("모공 케어");
            assertThat(result.volumeValue()).isEqualTo(200);
            assertThat(result.volumeUnit()).isEqualTo("ml");
            assertThat(result.storageCondition()).isEqualTo("서늘한 곳 보관");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_ProductNotFoundException이_발생한다() {
            // given
            given(productPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.findById(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품_등록 {

        @Test
        void 올바른_상품_정보를_입력하면_정상적으로_데이터에_등록된다() {
            // given
            RegisterProductCommand command = new ProductDtoBuilder().build().toCommand();
            // 첫 번째 save: INSERT → productId 할당된 Product 반환
            // 두 번째 save: SKU 코드 설정 후 UPDATE
            given(productPort.save(any(Product.class))).willReturn(defaultProduct);
            given(categoryPort.findById(command.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypePort.findById(command.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productPort.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);

            // when
            ProductResult result = productService.register(command);

            // then
            assertThat(result.productId()).isEqualTo(1L);
            assertThat(result.skuCode()).isEqualTo("P000001");
            assertThat(result.brandName()).isEqualTo(command.brandName());
            assertThat(result.productName()).isEqualTo(command.productName());
        }

        @Test
        void 상품이_등록되면_PK_기반_SKU_코드가_부여된다() {
            // given
            RegisterProductCommand command = new ProductDtoBuilder().build().toCommand();

            // INSERT 결과를 시뮬레이션: productId는 있지만 skuCode는 아직 없는 Product
            Product productAfterInsert = Product.reconstitute(
                    42L, null, "BIO", "하이드라비오 토너", 15000, TemperatureType.ROOM,
                    defaultCategory, defaultProductType,
                    new ProductInfoTestBuilder().volume(150).unit("ml").build()
            );

            given(categoryPort.findById(command.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypePort.findById(command.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productPort.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);
            given(productPort.save(any(Product.class)))
                    .willReturn(productAfterInsert)               // 첫 번째 save: INSERT
                    .willAnswer(inv -> inv.getArgument(0));       // 두 번째 save: UPDATE (skuCode 설정 후)

            // when
            ProductResult result = productService.register(command);

            // then
            assertThat(result.skuCode()).isEqualTo("P000042");
        }

        @Test
        void 존재하지_않는_카테고리_ID로_상품_등록을_시도하면_CategoryNotFoundException이_발생한다() {
            // given
            RegisterProductCommand command = new ProductDtoBuilder().build().toCommand();
            given(categoryPort.findById(command.categoryId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.register(command))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        void 존재하지_않는_상품_타입_ID로_상품_등록을_시도하면_ProductTypeNotFoundException이_발생한다() {
            // given
            RegisterProductCommand command = new ProductDtoBuilder().build().toCommand();
            given(categoryPort.findById(command.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypePort.findById(command.productTypeId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.register(command))
                    .isInstanceOf(ProductTypeNotFoundException.class);
        }

        @Test
        void 동일한_스펙을_가진_제품이_이미_존재하면_DuplicateProductException이_발생한다() {
            // given
            RegisterProductCommand command = new ProductDtoBuilder().build().toCommand();

            given(categoryPort.findById(command.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypePort.findById(command.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productPort.existsDuplicateProduct(
                    command.brandName(), command.productName(),
                    defaultCategory.getCategoryId(), defaultProductType.getProductTypeId(),
                    command.volumeValue(), command.volumeUnit()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.register(command))
                    .isInstanceOf(DuplicateProductException.class);
        }
    }

    @Nested
    class 상품_수정 {

        @Test
        void 올바른_수정_정보로_상품을_수정하면_수정_가능한_필드가_변경되고_SKU와_용량은_보존된다() {
            // given
            UpdateProductCommand command = new UpdateProductCommand(
                    "리뉴얼 하이드라비오 토너", 20000, TemperatureType.COOL,
                    "지성", "모공 케어", "정제수, 나이아신아마이드", "직사광선 주의", "서늘한 곳 보관"
            );

            given(productPort.findById(1L)).willReturn(Optional.of(defaultProduct));
            given(productPort.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            ProductResult result = productService.update(1L, command);

            // then
            assertThat(result.productName()).isEqualTo("리뉴얼 하이드라비오 토너");
            assertThat(result.productPrice()).isEqualTo(20000);
            assertThat(result.temperatureType()).isEqualTo(TemperatureType.COOL);
            assertThat(result.skinType()).isEqualTo("지성");
            assertThat(result.functionType()).isEqualTo("모공 케어");
            assertThat(result.skuCode()).isEqualTo("P000001");
            assertThat(result.volumeValue()).isEqualTo(150);
            assertThat(result.volumeUnit()).isEqualTo("ml");
        }

        @Test
        void 상품_정보가_없는_필드는_빈_문자열로_처리되어도_수정이_정상_완료된다() {
            // given
            UpdateProductCommand command = new UpdateProductCommand(
                    "심플 토너", 9900, TemperatureType.ROOM,
                    null, null, null, null, null
            );

            given(productPort.findById(1L)).willReturn(Optional.of(defaultProduct));
            given(productPort.save(any(Product.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            ProductResult result = productService.update(1L, command);

            // then
            assertThat(result.productName()).isEqualTo("심플 토너");
            assertThat(result.productPrice()).isEqualTo(9900);
            assertThat(result.skinType()).isEqualTo("");
            assertThat(result.functionType()).isEqualTo("");
        }

        @Test
        void 존재하지_않는_상품_ID로_수정을_시도하면_ProductNotFoundException이_발생한다() {
            // given
            UpdateProductCommand command = new UpdateProductCommand(
                    "리뉴얼 토너", 20000, TemperatureType.ROOM,
                    null, null, null, null, null
            );

            given(productPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.update(999L, command))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품_삭제 {

        @Test
        void 존재하는_상품을_삭제하면_포트의_delete가_호출된다() {
            // given
            given(productPort.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            productService.delete(1L);

            // then
            verify(productPort).delete(defaultProduct);
        }

        @Test
        void 존재하지_않는_상품_ID로_삭제를_시도하면_ProductNotFoundException이_발생한다() {
            // given
            given(productPort.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.delete(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }
}