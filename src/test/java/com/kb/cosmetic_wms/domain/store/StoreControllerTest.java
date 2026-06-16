package com.kb.cosmetic_wms.domain.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.store.controller.StoreController;
import com.kb.cosmetic_wms.domain.store.dto.StoreCreateRequestDto;
import com.kb.cosmetic_wms.domain.store.dto.StoreResponseDto;
import com.kb.cosmetic_wms.domain.store.exception.StoreErrorCode;
import com.kb.cosmetic_wms.domain.store.exception.StoreNotFoundException;
import com.kb.cosmetic_wms.domain.store.service.StoreService;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.STORE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.STORE_CREATE_REQUEST;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.STORE_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StoreController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
public class StoreControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

    @Test
    @WithMockUser
    void 올바른_가맹점_정보를_입력하면_가맹점_등록에_성공하고_API_문서가_생성된다() throws Exception {
        // given
        StoreCreateRequestDto requestDto = new StoreCreateRequestDto("서울 성수점", "서울시 성동구 성수동");
        StoreResponseDto responseDto = new StoreResponseDto(1L, "서울 성수점", "서울시 성동구 성수동");

        given(storeService.register(any(StoreCreateRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeName").value("서울 성수점"))
                .andExpect(jsonPath("$.address").value("서울시 성동구 성수동"))
                .andDo(document("store-create-success",
                        buildParams(STORE, "WMS 가맹점 등록", STORE_CREATE_REQUEST, STORE_RESPONSE),
                        createRequestFields(getStoreCreateRequestFields()),
                        createResponseFields(getStoreResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하는_가맹점_ID로_조회하면_200_OK와_함께_정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        Long storeId = 1L;
        StoreResponseDto responseDto = new StoreResponseDto(storeId, "서울 성수점", "서울시 성동구 성수동");

        given(storeService.findById(storeId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.storeName").value("서울 성수점"))
                .andDo(document("store-get-success",
                        buildParams(STORE, "가맹점 단건 조회", null, STORE_RESPONSE),
                        createResponseFields(getStoreResponseFields())
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_가맹점_ID로_조회하면_404_NOT_FOUND를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        Long invalidStoreId = 999L;

        given(storeService.findById(invalidStoreId)).willThrow(new StoreNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", invalidStoreId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("STORE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(StoreErrorCode.STORE_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())
                .andDo(document("store-get-fail-not-found",
                        buildErrorParams(STORE, "가맹점 단건 조회"),
                        globalErrorResponseFields()
                ));
    }

    private static FieldDescriptor[] getStoreCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("storeName").description("등록할 가맹점 점포 명칭 (필수값)"),
                fieldWithPath("address").description("가맹점 물리 주소 (필수값)")
        };
    }

    private static FieldDescriptor[] getStoreResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("가맹점 고유 식별 번호 (PK)"),
                fieldWithPath("storeName").description("가맹점 점포 명칭"),
                fieldWithPath("address").description("가맹점 주소")
        };
    }

}
