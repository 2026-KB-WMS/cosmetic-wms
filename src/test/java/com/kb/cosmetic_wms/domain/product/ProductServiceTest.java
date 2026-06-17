package com.kb.cosmetic_wms.domain.product;

import com.kb.cosmetic_wms.domain.product.dto.ProductCreateRequestDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductSummaryResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductUpdateRequestDto;
import com.kb.cosmetic_wms.domain.product.entity.Category;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.entity.ProductType;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.product.exception.*;
import com.kb.cosmetic_wms.domain.product.fixture.ProductDtoBuilder;
import com.kb.cosmetic_wms.domain.product.fixture.ProductInfoTestBuilder;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;
import com.kb.cosmetic_wms.domain.product.repository.CategoryRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductRepository;
import com.kb.cosmetic_wms.domain.product.repository.ProductTypeRepository;
import com.kb.cosmetic_wms.domain.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductTypeRepository productTypeRepository;

    private Category defaultCategory;
    private ProductType defaultProductType;
    private Product defaultProduct;

    @BeforeEach
    void setUp() {
        defaultCategory = Category.create("SKN", "스킨케어");
        defaultProductType = ProductType.create("TON", "토너");
        defaultProduct = new ProductTestBuilder()
                .category(defaultCategory)
                .productType(defaultProductType)
                .build();
        ReflectionTestUtils.setField(defaultProduct, "id", 1L);
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
                    .sequence(1)
                    .build();
            ReflectionTestUtils.setField(secondProduct, "id", 2L);

            given(productRepository.findAll()).willReturn(List.of(defaultProduct, secondProduct));

            // when
            List<ProductSummaryResponseDto> result = productService.getProducts();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(0).skuCode()).isEqualTo("BIO-SKN-TON-150-0001");
            assertThat(result.get(1).id()).isEqualTo(2L);
            assertThat(result.get(1).brandName()).isEqualTo("LABO");
        }

        @Test
        void 등록된_상품이_없으면_빈_목록을_반환한다() {
            // given
            given(productRepository.findAll()).willReturn(List.of());

            // when
            List<ProductSummaryResponseDto> result = productService.getProducts();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 목록_응답에는_productInfo가_포함되지_않고_핵심_필드만_반환된다() {
            // given
            given(productRepository.findAll()).willReturn(List.of(defaultProduct));

            // when
            List<ProductSummaryResponseDto> result = productService.getProducts();

            // then
            ProductSummaryResponseDto summary = result.get(0);
            assertThat(summary.id()).isEqualTo(1L);
            assertThat(summary.skuCode()).isEqualTo("BIO-SKN-TON-150-0001");
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
            given(productRepository.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            ProductDetailResponseDto result = productService.getProduct(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.skuCode()).isEqualTo("BIO-SKN-TON-150-0001");
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
            ReflectionTestUtils.setField(product, "id", 1L);

            given(productRepository.findById(1L)).willReturn(Optional.of(product));

            // when
            ProductDetailResponseDto result = productService.getProduct(1L);

            // then
            assertThat(result.productInfo().skinType()).isEqualTo("지성");
            assertThat(result.productInfo().functionType()).isEqualTo("모공 케어");
            assertThat(result.productInfo().volumeValue()).isEqualTo(200);
            assertThat(result.productInfo().volumeUnit()).isEqualTo("ml");
            assertThat(result.productInfo().storageCondition()).isEqualTo("서늘한 곳 보관");
        }

        @Test
        void 존재하지_않는_ID로_조회하면_ProductNotFoundException이_발생한다() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품_등록 {

        @Test
        void 올바른_상품_정보를_입력하면_정상적으로_데이터에_등록된다() {
            // given
            ProductCreateRequestDto request = new ProductDtoBuilder().build();

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(
                    request.brandName(), request.productName(),
                    defaultCategory, defaultProductType,
                    request.productInfo().volume().value(), request.productInfo().volume().unit()))
                    .willReturn(false);
            given(productRepository.findNextSequence(
                    defaultCategory.getCategoryCode(), defaultProductType.getTypeCode(),
                    request.productInfo().volume().value(), request.brandName()))
                    .willReturn(1);
            given(productRepository.save(any(Product.class))).willReturn(defaultProduct);

            // when
            ProductDetailResponseDto result = productService.register(request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.skuCode()).isEqualTo("BIO-SKN-TON-150-0001");
            assertThat(result.brandName()).isEqualTo(request.brandName());
            assertThat(result.productName()).isEqualTo(request.productName());
        }

        @Test
        void 화장품_상세_정보나_용량_단위가_다양해도_매핑_규격에_맞게_정상_저장된다() {
            // given - 50g 크림 등록
            Product productGrams = new ProductTestBuilder()
                    .category(defaultCategory)
                    .productType(defaultProductType)
                    .productInfo(new ProductInfoTestBuilder().volume(50).unit("g").build())
                    .sequence(1)
                    .build();
            ReflectionTestUtils.setField(productGrams, "id", 2L);

            ProductCreateRequestDto request = new ProductDtoBuilder()
                    .volume(50).unit("g")
                    .build();

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);
            given(productRepository.findNextSequence(any(), any(), anyInt(), any())).willReturn(1);
            given(productRepository.save(captor.capture())).willReturn(productGrams);

            // when
            productService.register(request);

            // then
            Product saved = captor.getValue();
            assertThat(saved.getSkuCode()).isEqualTo("BIO-SKN-TON-50-0001");
            assertThat(saved.getProductInfo().getVolume().value()).isEqualTo(50);
            assertThat(saved.getProductInfo().getVolume().unit()).isEqualTo("g");
        }

        @Test
        void 동일한_조건에서_새_상품을_등록하면_SKU_코드의_순번이_순차적으로_증가하여_부여된다() {
            // given - findNextSequence가 2를 반환 → 이미 sequence 1인 상품이 존재하는 상황
            ProductCreateRequestDto request = new ProductDtoBuilder().build();
            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);
            given(productRepository.findNextSequence(any(), any(), anyInt(), any())).willReturn(2);
            given(productRepository.save(captor.capture())).willReturn(defaultProduct);

            // when
            productService.register(request);

            // then
            assertThat(captor.getValue().getSkuCode()).isEqualTo("BIO-SKN-TON-150-0002");
        }

        @Test
        void 특정_카테고리나_상품_타입의_첫_상품_등록_시_SKU_코드_순번이_0001로_초기화되어_발급된다() {
            // given - findNextSequence가 1을 반환 → 해당 그룹 내 첫 번째 상품
            ProductCreateRequestDto request = new ProductDtoBuilder().build();
            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);
            given(productRepository.findNextSequence(any(), any(), anyInt(), any())).willReturn(1);
            given(productRepository.save(captor.capture())).willReturn(defaultProduct);

            // when
            productService.register(request);

            // then
            assertThat(captor.getValue().getSkuCode()).endsWith("-0001");
        }

        @Test
        void 존재하지_않는_카테고리_ID로_상품_등록을_시도하면_CategoryNotFoundException이_발생한다() {
            // given
            ProductCreateRequestDto request = new ProductDtoBuilder().build();

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.register(request))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        void 존재하지_않는_상품_타입_ID로_상품_등록을_시도하면_ProductTypeNotFoundException이_발생한다() {
            // given
            ProductCreateRequestDto request = new ProductDtoBuilder().build();

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.register(request))
                    .isInstanceOf(ProductTypeNotFoundException.class);
        }

        @Test
        void SKU_코드_생성_과정에서_순번이_허용_최대치를_초과하면_SkuSequenceOverflowException이_발생한다() {
            // 최대치 = 9999, findNextSequence가 10000을 반환하면 초과
            ProductCreateRequestDto request = new ProductDtoBuilder().build();

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(any(), any(), any(), any(), anyInt(), anyString()))
                    .willReturn(false);
            given(productRepository.findNextSequence(any(), any(), anyInt(), any())).willReturn(10000);

            // when & then
            assertThatThrownBy(() -> productService.register(request))
                    .isInstanceOf(SkuSequenceOverflowException.class);
        }

        @Test
        void 상품_생성에서_동일한_스펙을_가진_제품이_이미_존재하면_DuplicateProductException이_발생한다() {
            // given
            ProductCreateRequestDto request = new ProductDtoBuilder().build();

            given(categoryRepository.findById(request.categoryId())).willReturn(Optional.of(defaultCategory));
            given(productTypeRepository.findById(request.productTypeId())).willReturn(Optional.of(defaultProductType));
            given(productRepository.existsDuplicateProduct(
                    request.brandName(), request.productName(),
                    defaultCategory, defaultProductType,
                    request.productInfo().volume().value(), request.productInfo().volume().unit()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> productService.register(request))
                    .isInstanceOf(DuplicateProductException.class);
        }
    }

    @Nested
    class 상품_수정 {

        @Test
        void 올바른_수정_정보로_상품을_수정하면_수정_가능한_필드가_변경되고_SKU와_용량은_보존된다() {
            // given
            ProductUpdateRequestDto request = new ProductUpdateRequestDto(
                    "리뉴얼 하이드라비오 토너",
                    20000,
                    TemperatureType.COOL,
                    new ProductUpdateRequestDto.ProductInfoUpdateRequest(
                            "지성", "모공 케어", "정제수, 나이아신아마이드", "직사광선 주의", "서늘한 곳 보관")
            );

            given(productRepository.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            ProductDetailResponseDto result = productService.updateProduct(1L, request);

            // then
            assertThat(result.productName()).isEqualTo("리뉴얼 하이드라비오 토너");
            assertThat(result.productPrice()).isEqualTo(20000);
            assertThat(result.temperatureType()).isEqualTo(TemperatureType.COOL);
            assertThat(result.productInfo().skinType()).isEqualTo("지성");
            assertThat(result.productInfo().functionType()).isEqualTo("모공 케어");
            assertThat(result.skuCode()).isEqualTo("BIO-SKN-TON-150-0001");
            assertThat(result.productInfo().volumeValue()).isEqualTo(150);
            assertThat(result.productInfo().volumeUnit()).isEqualTo("ml");
        }

        @Test
        void 상품_정보가_없는_필드는_빈_문자열로_처리되어도_수정이_정상_완료된다() {
            // given
            ProductUpdateRequestDto request = new ProductUpdateRequestDto(
                    "심플 토너",
                    9900,
                    TemperatureType.ROOM,
                    new ProductUpdateRequestDto.ProductInfoUpdateRequest(
                            null, null, null, null, null)
            );

            given(productRepository.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            ProductDetailResponseDto result = productService.updateProduct(1L, request);

            // then
            assertThat(result.productName()).isEqualTo("심플 토너");
            assertThat(result.productPrice()).isEqualTo(9900);
            assertThat(result.productInfo().skinType()).isEqualTo("");
            assertThat(result.productInfo().functionType()).isEqualTo("");
        }

        @Test
        void 존재하지_않는_상품_ID로_수정을_시도하면_ProductNotFoundException이_발생한다() {
            // given
            ProductUpdateRequestDto request = new ProductUpdateRequestDto(
                    "리뉴얼 토너", 20000, TemperatureType.ROOM,
                    new ProductUpdateRequestDto.ProductInfoUpdateRequest(null, null, null, null, null)
            );

            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 상품_삭제 {

        @Test
        void 존재하는_상품을_삭제하면_레포지토리_delete가_호출된다() {
            // given
            given(productRepository.findById(1L)).willReturn(Optional.of(defaultProduct));

            // when
            productService.deleteProduct(1L);

            // then
            verify(productRepository).delete(defaultProduct);
        }

        @Test
        void 존재하지_않는_상품_ID로_삭제를_시도하면_ProductNotFoundException이_발생한다() {
            // given
            given(productRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ProductNotFoundException.class)
                    .hasMessage(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage());
        }
    }
}
