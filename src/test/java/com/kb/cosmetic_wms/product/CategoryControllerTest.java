package com.kb.cosmetic_wms.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.product.category.adapter.in.web.CategoryController;
import com.kb.cosmetic_wms.product.category.adapter.in.web.RegisterCategoryRequest;
import com.kb.cosmetic_wms.product.category.application.port.in.CategoryResult;
import com.kb.cosmetic_wms.product.category.application.port.in.DeleteCategoryUseCase;
import com.kb.cosmetic_wms.product.category.application.port.in.FindCategoryUseCase;
import com.kb.cosmetic_wms.product.category.application.port.in.RegisterCategoryUseCase;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryErrorCode;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryInUseException;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.product.category.domain.exception.DuplicateCategoryException;
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
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.CATEGORY;
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

@WebMvcTest(controllers = CategoryController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class CategoryControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterCategoryUseCase registerCategoryUseCase;

    @MockitoBean
    private FindCategoryUseCase findCategoryUseCase;

    @MockitoBean
    private DeleteCategoryUseCase deleteCategoryUseCase;

    // --- 목록 조회 ---

    @Test
    @WithMockUser
    void 카테고리_목록_조회_성공_시_200_OK와_전체_목록을_반환하고_API_문서가_생성된다() throws Exception {
        // given
        List<CategoryResult> results = List.of(
                new CategoryResult(1L, "SKN", "스킨케어"),
                new CategoryResult(2L, "MKP", "메이크업")
        );
        given(findCategoryUseCase.findAll()).willReturn(results);

        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1L))
                .andExpect(jsonPath("$[0].categoryCode").value("SKN"))
                .andExpect(jsonPath("$[0].categoryName").value("스킨케어"))
                .andExpect(jsonPath("$[1].categoryId").value(2L))
                .andDo(document("category-get-all-success",
                        buildParams(CATEGORY, "카테고리 목록 조회", null, CATEGORY_RESPONSE),
                        createListResponseFields(getCategoryResponseFields())
                ));
    }

    // --- 단건 조회 ---

    @Test
    @WithMockUser
    void 존재하는_카테고리_ID로_조회하면_200_OK와_카테고리_정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        given(findCategoryUseCase.findById(1L)).willReturn(new CategoryResult(1L, "SKN", "스킨케어"));

        // when & then
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.categoryCode").value("SKN"))
                .andExpect(jsonPath("$.categoryName").value("스킨케어"))
                .andDo(document("category-get-one-success",
                        buildParams(CATEGORY, "카테고리 단건 조회", null, CATEGORY_RESPONSE),
                        createResponseFields(getCategoryResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_카테고리_ID로_조회하면_404_NOT_FOUND를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        given(findCategoryUseCase.findById(999L)).willThrow(new CategoryNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/categories/{categoryId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("category-get-one-fail-not-found",
                        buildErrorParams(CATEGORY, "카테고리 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    // --- 등록 ---

    @Test
    @WithMockUser
    void 올바른_카테고리_정보를_입력하면_201_Created와_등록된_카테고리를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        RegisterCategoryRequest request = new RegisterCategoryRequest("SKN", "스킨케어");
        given(registerCategoryUseCase.register(any())).willReturn(new CategoryResult(1L, "SKN", "스킨케어"));

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1L))
                .andExpect(jsonPath("$.categoryCode").value("SKN"))
                .andDo(document("category-create-success",
                        buildParams(CATEGORY, "카테고리 등록", CATEGORY_CREATE_REQUEST, CATEGORY_RESPONSE),
                        createRequestFields(getCategoryCreateRequestFields()),
                        createResponseFields(getCategoryResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 카테고리_코드가_공백이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        RegisterCategoryRequest invalidRequest = new RegisterCategoryRequest("", "스킨케어");

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("category-create-fail-validation",
                        buildErrorParams(CATEGORY, "카테고리 등록"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 중복된_카테고리_코드로_등록하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        RegisterCategoryRequest request = new RegisterCategoryRequest("SKN", "스킨케어 2");
        given(registerCategoryUseCase.register(any())).willThrow(new DuplicateCategoryException());

        // when & then
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_CATEGORY"))
                .andExpect(jsonPath("$.message").value(CategoryErrorCode.DUPLICATE_CATEGORY.getMessage()))
                .andDo(document("category-create-fail-duplicate",
                        buildErrorParams(CATEGORY, "카테고리 등록"),
                        globalErrorResponseFields()
                ));
    }

    // --- 삭제 ---

    @Test
    @WithMockUser
    void 참조_상품이_없는_카테고리를_삭제하면_204_No_Content를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        doNothing().when(deleteCategoryUseCase).delete(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("category-delete-success",
                        buildParams(CATEGORY, "카테고리 삭제")
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_카테고리_ID로_삭제하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        doThrow(new CategoryNotFoundException()).when(deleteCategoryUseCase).delete(999L);

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{categoryId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(CategoryErrorCode.CATEGORY_NOT_FOUND.getMessage()))
                .andDo(document("category-delete-fail-not-found",
                        buildErrorParams(CATEGORY, "카테고리 삭제"),
                        globalErrorResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 참조_상품이_존재하는_카테고리를_삭제하면_409_Conflict를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        doThrow(new CategoryInUseException()).when(deleteCategoryUseCase).delete(1L);

        // when & then
        mockMvc.perform(delete("/api/v1/categories/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CATEGORY_IN_USE"))
                .andExpect(jsonPath("$.message").value(CategoryErrorCode.CATEGORY_IN_USE.getMessage()))
                .andDo(document("category-delete-fail-in-use",
                        buildErrorParams(CATEGORY, "카테고리 삭제"),
                        globalErrorResponseFields()
                ));
    }

    // --- REST Docs 필드 기술자 ---

    private static FieldDescriptor[] getCategoryResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 고유 식별 번호 (PK)"),
                fieldWithPath("categoryCode").type(JsonFieldType.STRING).description("카테고리 코드 (영문 대문자 3자리)"),
                fieldWithPath("categoryName").type(JsonFieldType.STRING).description("카테고리 이름")
        };
    }

    private static FieldDescriptor[] getCategoryCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("categoryCode").type(JsonFieldType.STRING).description("카테고리 코드 (영문 대문자 3자리, 필수)"),
                fieldWithPath("categoryName").type(JsonFieldType.STRING).description("카테고리 이름 (필수)")
        };
    }
}