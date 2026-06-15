package com.kb.cosmetic_wms.domain.partner;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.partner.controller.PartnerController;
import com.kb.cosmetic_wms.domain.partner.dto.PartnerCreateRequestDto;
import com.kb.cosmetic_wms.domain.partner.dto.PartnerResponseDto;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.domain.partner.service.PartnerService;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PartnerController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
public class PartnerControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerService partnerService;

    @Test
    @WithMockUser
    void 올바른_협력사_정보를_입력하면_등록에_성공하고_API_문서가_생성된다() throws Exception {
        PartnerCreateRequestDto requestDto = new PartnerCreateRequestDto("아모레퍼시픽", PartnerType.VENDOR, "120-00-12345");
        PartnerResponseDto responseDto = new PartnerResponseDto(1L, "아모레퍼시픽", PartnerType.VENDOR, "120-00-12345");

        given(partnerService.register(any(PartnerCreateRequestDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/partners")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andDo(document("partner-create-success",
                        ResourceSnippetParameters.builder()
                                .tag("Partner API")
                                .summary("WMS 기초 가맹점 협력사 등록"),
                        getPartnerCreateRequestFields(),
                        getPartnerResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 존재하는_ID로_조회하면_200_OK와_함께_정보를_반환하고_API_문서가_생성된다() throws Exception {
        Long partnerId = 1L;
        PartnerResponseDto responseDto = new PartnerResponseDto(partnerId, "아모레퍼시픽", PartnerType.VENDOR, "120-00-12345");

        given(partnerService.findById(partnerId)).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/partners/{partnerId}", partnerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andDo(document("partner-get-success",
                        ResourceSnippetParameters.builder()
                                .tag("Partner API")
                                .summary("협력사 단건 조회"),
                        getPartnerResponseFields()
                ));
    }

    @Test
    @WithMockUser
    void 존재하지_않는_협력사_ID로_조회하면_404_NOT_FOUND를_반환하고_에러응답이_문서화된다() throws Exception {
        // given
        Long invalidPartnerId = 999L;

        given(partnerService.findById(invalidPartnerId)).willThrow(new PartnerNotFoundException());

        // when & then
        mockMvc.perform(get("/api/v1/partners/{partnerId}", invalidPartnerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())

                .andExpect(jsonPath("$.errorCode").value("PARTNER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(PartnerErrorCode.PARTNER_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.timestamp").exists())

                .andDo(document("partner-get-fail-not-found",
                        ResourceSnippetParameters.builder()
                                .tag("Partner API")
                                .summary("협력사 단건 조회 실패 - 존재하지 않는 ID")
                                .description("데이터에 존재하지 않는 협력사 고유 번호를 조회했을 때 발생하는 404 에러 명세입니다."),
                        getGlobalErrorResponseFields()
                ));
    }

    private static ResponseFieldsSnippet getGlobalErrorResponseFields() {
        return responseFields(
                fieldWithPath("errorCode").description("서버 내부 정의 에러 식별 코드"),
                fieldWithPath("message").description("에러 발생 상세 사유 메시지"),
                fieldWithPath("timestamp").description("에러 발생 시각")
        );
    }

    private static RequestFieldsSnippet getPartnerCreateRequestFields() {
        return requestFields(
                fieldWithPath("partnerName").description("협력사 회사 상호명 (필수값)"),
                fieldWithPath("partnerType").description("협력사 타입 (VENDOR, HEADQUARTER, BRANCH)"),
                fieldWithPath("businessNumber").description("사업자 등록 번호 (필수값)")
        );
    }

    private static ResponseFieldsSnippet getPartnerResponseFields() {
        return responseFields(
                fieldWithPath("id").description("협력사 고유 식별 번호 (PK)"),
                fieldWithPath("partnerName").description("협력사 상호명"),
                fieldWithPath("partnerType").description("협력사 타입 (VENDOR, HEADQUARTER, BRANCH)"), // 🌟 추가!
                fieldWithPath("businessNumber").description("사업자 등록 번호")
        );
    }
}
