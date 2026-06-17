package com.kb.cosmetic_wms.domain.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.product.controller.ProductController;
import com.kb.cosmetic_wms.domain.product.dto.ProductCreateRequestDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductDetailResponseDto.ProductInfoResponse;
import com.kb.cosmetic_wms.domain.product.dto.ProductSummaryResponseDto;
import com.kb.cosmetic_wms.domain.product.dto.ProductUpdateRequestDto;
import com.kb.cosmetic_wms.domain.product.enums.TemperatureType;
import com.kb.cosmetic_wms.domain.product.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.domain.product.exception.DuplicateProductException;
import com.kb.cosmetic_wms.domain.product.exception.ProductErrorCode;
import com.kb.cosmetic_wms.domain.product.exception.ProductNotFoundException;
import com.kb.cosmetic_wms.domain.product.fixture.ProductDtoBuilder;
import com.kb.cosmetic_wms.domain.product.service.ProductService;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.PRODUCT;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class ProductControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    // --- 목록 조회 ---

    @Test
    @WithMockUser
    void 상품_목록_조회_성공_시_200_OK와_전체_목록을_반환하고_API_문서가_생성된다() throws Exception {
        // given
        List<ProductSummaryResponseDto> responseList = List.of(
                new ProductSummaryResponseDto(1L, "BIO-SKN-TON-150-0001", "BIO", "하이드라비오 토너", 15000, TemperatureType.ROOM),
                new ProductSummaryResponseDto(2L, "LABO-SKN-TON-200-0001", "LABO", "라보 수분 토너", 25000, TemperatureType.COOL)
        );
        given(productService.getProducts()).willReturn(responseList);

        // when & then
        mockMvc.perform(get("/api/v1/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].skuCode").value("BIO-SKN-TON-150-0001"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andDo(document("product-get-all-success",
                        buildParams(PRODUCT, "상품 목록 조회", null, PRODUCT_SUMMARY_RESPONSE),
                        createListResponseFields(getProductSummaryResponseFields())
                ));
    }

    // --- 상세 조회 ---

    @Test
    @WithMockUser
    void 존재하는_상품_ID로_조회하면_200_OK와_함께_상세정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        ProductDetailResponseDto response = buildDetailResponse();
        given(productService.getProduct(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/products/{productId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.skuCode").value("BIO-SKN-TON-150-0001"))
                .andExpect(jsonPath("$.productInfo.skinType").value("건성"))
                .andExpect(jsonPath("$.productInfo.volumeValue").value(150))
                .andExpect(jsonPath("$.productInfo.volumeUnit").value("ml"))
                .andDo(document("product-get-one-success",
                        buildParams(PRODUCT, "상품 상세 조회", null, PRODUCT_DETAIL_RESPONSE),
                        createResponseFields(getProductDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_ID로_조회하면_404_NOT_FOUND를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        given(productService.getProduct(999L)).willThrow(new ProductNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/products/{productId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("product-get-one-fail-not-found",
                        buildErrorParams(PRODUCT, "상품 상세 조회"),
                        globalErrorResponseFields()
                ));
    }

    // --- 상품 등록 ---

    @Test
    @WithMockUser
    void 올바른_상품_정보를_입력하면_등록에_성공하고_201_Created와_API_문서가_생성된다() throws Exception {
        // given
        ProductCreateRequestDto request = new ProductDtoBuilder().build();
        ProductDetailResponseDto response = buildDetailResponse();
        given(productService.register(any(ProductCreateRequestDto.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.skuCode").value("BIO-SKN-TON-150-0001"))
                .andDo(document("product-create-success",
                        buildParams(PRODUCT, "상품 등록", PRODUCT_CREATE_REQUEST, PRODUCT_DETAIL_RESPONSE),
                        createRequestFields(getProductCreateRequestFields()),
                        createResponseFields(getProductDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 브랜드명이_공백이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        ProductCreateRequestDto invalidRequest = new ProductDtoBuilder().brandName("").build();

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("product-create-fail-validation",
                        buildErrorParams(PRODUCT, "상품 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 동일한_스펙의_상품이_이미_존재하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        ProductCreateRequestDto request = new ProductDtoBuilder().build();
        given(productService.register(any(ProductCreateRequestDto.class))).willThrow(new DuplicateProductException());

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PRODUCT"))
                .andExpect(jsonPath("$.message").value(ProductErrorCode.DUPLICATE_PRODUCT.getMessage()))
                .andDo(document("product-create-fail-duplicate",
                        buildErrorParams(PRODUCT, "상품 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_카테고리_ID로_등록하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        ProductCreateRequestDto request = new ProductDtoBuilder().build();
        given(productService.register(any(ProductCreateRequestDto.class))).willThrow(new CategoryNotFoundException());

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductErrorCode.CATEGORY_NOT_FOUND.getMessage()))
                .andDo(document("product-create-fail-category-not-found",
                        buildErrorParams(PRODUCT, "상품 등록"),
                        globalErrorResponseFields()
                ));
    }

    // --- 상품 수정 ---

    @Test
    @WithMockUser
    void 올바른_수정_정보를_입력하면_200_OK와_수정된_상품_정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        ProductUpdateRequestDto request = buildUpdateRequest();
        ProductDetailResponseDto response = new ProductDetailResponseDto(
                1L, "BIO-SKN-TON-150-0001", "BIO", "리뉴얼 하이드라비오 토너",
                20000, TemperatureType.COOL,
                new ProductInfoResponse("지성", "모공 케어", 150, "ml",
                        "정제수, 나이아신아마이드", "직사광선 주의", "서늘한 곳 보관")
        );
        given(productService.updateProduct(eq(1L), any(ProductUpdateRequestDto.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/products/{productId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("리뉴얼 하이드라비오 토너"))
                .andExpect(jsonPath("$.productPrice").value(20000))
                .andExpect(jsonPath("$.skuCode").value("BIO-SKN-TON-150-0001"))
                .andExpect(jsonPath("$.productInfo.volumeValue").value(150))
                .andDo(document("product-update-success",
                        buildParams(PRODUCT, "상품 수정", PRODUCT_UPDATE_REQUEST, PRODUCT_DETAIL_RESPONSE),
                        createRequestFields(getProductUpdateRequestFields()),
                        createResponseFields(getProductDetailResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_ID로_수정을_시도하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        ProductUpdateRequestDto request = buildUpdateRequest();
        given(productService.updateProduct(eq(999L), any(ProductUpdateRequestDto.class)))
                .willThrow(new ProductNotFoundException());

        // when & then
        mockMvc.perform(put("/api/v1/products/{productId}", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andDo(document("product-update-fail-not-found",
                        buildErrorParams(PRODUCT, "상품 수정"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 수정_요청에서_상품명이_공백이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        ProductUpdateRequestDto invalidRequest = new ProductUpdateRequestDto(
                "", 20000, TemperatureType.ROOM,
                new ProductUpdateRequestDto.ProductInfoUpdateRequest(null, null, null, null, null)
        );

        // when & then
        mockMvc.perform(put("/api/v1/products/{productId}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andDo(document("product-update-fail-validation",
                        buildErrorParams(PRODUCT, "상품 수정"),
                        globalErrorResponseFields()
                ));
    }

    // --- 상품 삭제 ---

    @Test
    @WithMockUser
    void 존재하는_상품을_삭제하면_204_No_Content를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        doNothing().when(productService).deleteProduct(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{productId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("product-delete-success",
                        buildParams(PRODUCT, "상품 삭제")
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_ID로_삭제를_시도하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        doThrow(new ProductNotFoundException()).when(productService).deleteProduct(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/products/{productId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductErrorCode.PRODUCT_NOT_FOUND.getMessage()))
                .andDo(document("product-delete-fail-not-found",
                        buildErrorParams(PRODUCT, "상품 삭제"),
                        globalErrorResponseFields()
                ));
    }

    // --- 테스트 픽스처 ---

    private static ProductUpdateRequestDto buildUpdateRequest() {
        return new ProductUpdateRequestDto(
                "리뉴얼 하이드라비오 토너",
                20000,
                TemperatureType.COOL,
                new ProductUpdateRequestDto.ProductInfoUpdateRequest(
                        "지성", "모공 케어",
                        "정제수, 나이아신아마이드",
                        "직사광선 주의",
                        "서늘한 곳 보관"
                )
        );
    }

    private static ProductDetailResponseDto buildDetailResponse() {
        return new ProductDetailResponseDto(
                1L,
                "BIO-SKN-TON-150-0001",
                "BIO",
                "하이드라비오 토너",
                15000,
                TemperatureType.ROOM,
                new ProductInfoResponse(
                        "건성", "보습", 150, "ml",
                        "정제수, 글리세린, 폴리솔베이트20",
                        "직사광선 주의",
                        "상온보관"
                )
        );
    }

    // --- REST Docs 필드 기술자 ---

    private static FieldDescriptor[] getProductSummaryResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("상품 고유 식별 번호 (PK)"),
                fieldWithPath("skuCode").type(JsonFieldType.STRING).description("SKU 코드 (브랜드-카테고리-타입-용량-순번)"),
                fieldWithPath("brandName").type(JsonFieldType.STRING).description("브랜드명"),
                fieldWithPath("productName").type(JsonFieldType.STRING).description("상품명"),
                fieldWithPath("productPrice").type(JsonFieldType.NUMBER).description("상품 가격"),
                fieldWithPath("temperatureType").type(JsonFieldType.STRING).description("보관 온도 타입 (ROOM: 상온 / COOL: 냉장)")
        };
    }

    private static FieldDescriptor[] getProductDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER).description("상품 고유 식별 번호 (PK)"),
                fieldWithPath("skuCode").type(JsonFieldType.STRING).description("SKU 코드 (브랜드-카테고리-타입-용량-순번)"),
                fieldWithPath("brandName").type(JsonFieldType.STRING).description("브랜드명"),
                fieldWithPath("productName").type(JsonFieldType.STRING).description("상품명"),
                fieldWithPath("productPrice").type(JsonFieldType.NUMBER).description("상품 가격"),
                fieldWithPath("temperatureType").type(JsonFieldType.STRING).description("보관 온도 타입 (ROOM: 상온 / COOL: 냉장)"),
                fieldWithPath("productInfo").type(JsonFieldType.OBJECT).description("상품 상세 정보"),
                fieldWithPath("productInfo.skinType").type(JsonFieldType.STRING).description("피부 타입").optional(),
                fieldWithPath("productInfo.functionType").type(JsonFieldType.STRING).description("기능성 타입").optional(),
                fieldWithPath("productInfo.volumeValue").type(JsonFieldType.NUMBER).description("용량 수치"),
                fieldWithPath("productInfo.volumeUnit").type(JsonFieldType.STRING).description("용량 단위 (ml, g, ea, oz, fl.oz)"),
                fieldWithPath("productInfo.ingredients").type(JsonFieldType.STRING).description("전성분 정보").optional(),
                fieldWithPath("productInfo.cautions").type(JsonFieldType.STRING).description("사용 주의사항").optional(),
                fieldWithPath("productInfo.storageCondition").type(JsonFieldType.STRING).description("보관 조건").optional()
        };
    }

    private static FieldDescriptor[] getProductUpdateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("productName").type(JsonFieldType.STRING).description("상품명 (필수)"),
                fieldWithPath("productPrice").type(JsonFieldType.NUMBER).description("상품 가격 (0원 이상, 필수)"),
                fieldWithPath("temperatureType").type(JsonFieldType.STRING).description("보관 온도 타입 (ROOM: 상온 / COOL: 냉장, 필수)"),
                fieldWithPath("productInfo").type(JsonFieldType.OBJECT).description("상품 상세 정보 (필수) — SKU 구성 요소인 volume은 수정 불가"),
                fieldWithPath("productInfo.skinType").type(JsonFieldType.STRING).description("피부 타입 (선택)").optional(),
                fieldWithPath("productInfo.functionType").type(JsonFieldType.STRING).description("기능성 타입 (선택)").optional(),
                fieldWithPath("productInfo.ingredients").type(JsonFieldType.STRING).description("전성분 정보 (선택)").optional(),
                fieldWithPath("productInfo.cautions").type(JsonFieldType.STRING).description("사용 주의사항 (선택)").optional(),
                fieldWithPath("productInfo.storageCondition").type(JsonFieldType.STRING).description("보관 조건 (선택)").optional()
        };
    }

    private static FieldDescriptor[] getProductCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("brandName").type(JsonFieldType.STRING).description("브랜드명 (필수)"),
                fieldWithPath("productName").type(JsonFieldType.STRING).description("상품명 (필수)"),
                fieldWithPath("productPrice").type(JsonFieldType.NUMBER).description("상품 가격 (0원 이상, 필수)"),
                fieldWithPath("temperatureType").type(JsonFieldType.STRING).description("보관 온도 타입 (ROOM: 상온 / COOL: 냉장, 필수)"),
                fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID (필수)"),
                fieldWithPath("productTypeId").type(JsonFieldType.NUMBER).description("상품 타입 ID (필수)"),
                fieldWithPath("productInfo").type(JsonFieldType.OBJECT).description("상품 상세 정보 (필수)"),
                fieldWithPath("productInfo.skinType").type(JsonFieldType.STRING).description("피부 타입 (선택)").optional(),
                fieldWithPath("productInfo.functionType").type(JsonFieldType.STRING).description("기능성 타입 (선택)").optional(),
                fieldWithPath("productInfo.volume").type(JsonFieldType.OBJECT).description("용량 정보 (필수)"),
                fieldWithPath("productInfo.volume.value").type(JsonFieldType.NUMBER).description("용량 수치 (1 이상)"),
                fieldWithPath("productInfo.volume.unit").type(JsonFieldType.STRING).description("용량 단위 (ml, g, ea, oz, fl.oz)"),
                fieldWithPath("productInfo.ingredients").type(JsonFieldType.STRING).description("전성분 정보 (선택)").optional(),
                fieldWithPath("productInfo.cautions").type(JsonFieldType.STRING).description("사용 주의사항 (선택)").optional(),
                fieldWithPath("productInfo.storageCondition").type(JsonFieldType.STRING).description("보관 조건 (선택)").optional()
        };
    }
}
