package com.kb.cosmetic_wms.domain.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.product.producttype.adapter.in.web.ProductTypeController;
import com.kb.cosmetic_wms.product.producttype.adapter.in.web.RegisterProductTypeRequest;
import com.kb.cosmetic_wms.product.producttype.application.port.in.DeleteProductTypeUseCase;
import com.kb.cosmetic_wms.product.producttype.application.port.in.FindProductTypeUseCase;
import com.kb.cosmetic_wms.product.producttype.application.port.in.ProductTypeResult;
import com.kb.cosmetic_wms.product.producttype.application.port.in.RegisterProductTypeUseCase;
import com.kb.cosmetic_wms.product.producttype.domain.exception.DuplicateProductTypeException;
import com.kb.cosmetic_wms.product.producttype.domain.exception.ProductTypeErrorCode;
import com.kb.cosmetic_wms.product.producttype.domain.exception.ProductTypeInUseException;
import com.kb.cosmetic_wms.product.producttype.domain.exception.ProductTypeNotFoundException;
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
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.PRODUCT_TYPE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductTypeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class ProductTypeControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterProductTypeUseCase registerProductTypeUseCase;

    @MockitoBean
    private FindProductTypeUseCase findProductTypeUseCase;

    @MockitoBean
    private DeleteProductTypeUseCase deleteProductTypeUseCase;

    // --- 목록 조회 ---

    @Test
    @WithMockUser
    void 상품_타입_목록_조회_성공_시_200_OK와_전체_목록을_반환하고_API_문서가_생성된다() throws Exception {
        // given
        List<ProductTypeResult> results = List.of(
                new ProductTypeResult(1L, "TON", "토너"),
                new ProductTypeResult(2L, "CRM", "크림")
        );
        given(findProductTypeUseCase.findAll()).willReturn(results);

        // when & then
        mockMvc.perform(get("/api/v1/product-types")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productTypeId").value(1L))
                .andExpect(jsonPath("$[0].typeCode").value("TON"))
                .andExpect(jsonPath("$[0].typeName").value("토너"))
                .andExpect(jsonPath("$[1].productTypeId").value(2L))
                .andDo(document("product-type-get-all-success",
                        buildParams(PRODUCT_TYPE, "상품 타입 목록 조회", null, PRODUCT_TYPE_RESPONSE),
                        createListResponseFields(getProductTypeResponseFields())
                ));
    }

    // --- 단건 조회 ---

    @Test
    @WithMockUser
    void 존재하는_상품_타입_ID로_조회하면_200_OK와_상품_타입_정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        given(findProductTypeUseCase.findById(1L)).willReturn(new ProductTypeResult(1L, "TON", "토너"));

        // when & then
        mockMvc.perform(get("/api/v1/product-types/{productTypeId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productTypeId").value(1L))
                .andExpect(jsonPath("$.typeCode").value("TON"))
                .andExpect(jsonPath("$.typeName").value("토너"))
                .andDo(document("product-type-get-one-success",
                        buildParams(PRODUCT_TYPE, "상품 타입 단건 조회", null, PRODUCT_TYPE_RESPONSE),
                        createResponseFields(getProductTypeResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_타입_ID로_조회하면_404_NOT_FOUND를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        given(findProductTypeUseCase.findById(999L)).willThrow(new ProductTypeNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/product-types/{productTypeId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_TYPE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("product-type-get-one-fail-not-found",
                        buildErrorParams(PRODUCT_TYPE, "상품 타입 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    // --- 등록 ---

    @Test
    @WithMockUser
    void 올바른_상품_타입_정보를_입력하면_201_Created와_등록된_상품_타입을_반환하고_API_문서가_생성된다() throws Exception {
        // given
        RegisterProductTypeRequest request = new RegisterProductTypeRequest("TON", "토너");
        given(registerProductTypeUseCase.register(any())).willReturn(new ProductTypeResult(1L, "TON", "토너"));

        // when & then
        mockMvc.perform(post("/api/v1/product-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productTypeId").value(1L))
                .andExpect(jsonPath("$.typeCode").value("TON"))
                .andDo(document("product-type-create-success",
                        buildParams(PRODUCT_TYPE, "상품 타입 등록", PRODUCT_TYPE_CREATE_REQUEST, PRODUCT_TYPE_RESPONSE),
                        createRequestFields(getProductTypeCreateRequestFields()),
                        createResponseFields(getProductTypeResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 타입_코드가_공백이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        RegisterProductTypeRequest invalidRequest = new RegisterProductTypeRequest("", "토너");

        // when & then
        mockMvc.perform(post("/api/v1/product-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("product-type-create-fail-validation",
                        buildErrorParams(PRODUCT_TYPE, "상품 타입 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 중복된_타입_코드로_등록하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        RegisterProductTypeRequest request = new RegisterProductTypeRequest("TON", "토너 2");
        given(registerProductTypeUseCase.register(any())).willThrow(new DuplicateProductTypeException());

        // when & then
        mockMvc.perform(post("/api/v1/product-types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PRODUCT_TYPE"))
                .andExpect(jsonPath("$.message").value(ProductTypeErrorCode.DUPLICATE_PRODUCT_TYPE.getMessage()))
                .andDo(document("product-type-create-fail-duplicate",
                        buildErrorParams(PRODUCT_TYPE, "상품 타입 등록"),
                        globalErrorResponseFields()
                ));
    }

    // --- 삭제 ---

    @Test
    @WithMockUser
    void 참조_상품이_없는_상품_타입을_삭제하면_204_No_Content를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        doNothing().when(deleteProductTypeUseCase).delete(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/product-types/{productTypeId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("product-type-delete-success",
                        buildParams(PRODUCT_TYPE, "상품 타입 삭제")
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_상품_타입_ID로_삭제하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        doThrow(new ProductTypeNotFoundException()).when(deleteProductTypeUseCase).delete(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/product-types/{productTypeId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_TYPE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(ProductTypeErrorCode.PRODUCT_TYPE_NOT_FOUND.getMessage()))
                .andDo(document("product-type-delete-fail-not-found",
                        buildErrorParams(PRODUCT_TYPE, "상품 타입 삭제"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 참조_상품이_존재하는_상품_타입을_삭제하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        doThrow(new ProductTypeInUseException()).when(deleteProductTypeUseCase).delete(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/product-types/{productTypeId}", 1L)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_TYPE_IN_USE"))
                .andExpect(jsonPath("$.message").value(ProductTypeErrorCode.PRODUCT_TYPE_IN_USE.getMessage()))
                .andDo(document("product-type-delete-fail-in-use",
                        buildErrorParams(PRODUCT_TYPE, "상품 타입 삭제"),
                        globalErrorResponseFields()
                ));
    }

    // --- REST Docs 필드 기술자 ---

    private static FieldDescriptor[] getProductTypeResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("productTypeId").type(JsonFieldType.NUMBER).description("상품 타입 고유 식별 번호 (PK)"),
                fieldWithPath("typeCode").type(JsonFieldType.STRING).description("타입 코드 (영문 대문자 3자리)"),
                fieldWithPath("typeName").type(JsonFieldType.STRING).description("타입 이름")
        };
    }

    private static FieldDescriptor[] getProductTypeCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("typeCode").type(JsonFieldType.STRING).description("타입 코드 (영문 대문자 3자리, 필수)"),
                fieldWithPath("typeName").type(JsonFieldType.STRING).description("타입 이름 (필수)")
        };
    }
}