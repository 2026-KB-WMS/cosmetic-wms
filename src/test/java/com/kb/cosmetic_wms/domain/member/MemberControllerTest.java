package com.kb.cosmetic_wms.domain.member;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.member.controller.MemberController;
import com.kb.cosmetic_wms.domain.member.dto.MemberDetailResponseDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberLoginRequestDto;
import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequestDto;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import com.kb.cosmetic_wms.domain.member.fixture.MemberDtoBuilder;
import com.kb.cosmetic_wms.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.domain.member.fixture.MemberDocumentUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MemberController.class)
@ExtendWith(RestDocumentationExtension.class)
public class MemberControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    protected MemberService memberService;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    void 새로운_회원_정보를_입력하면_회원_가입에_성공하고_API_문서가_생성된다() throws Exception {
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest().build();
        MemberDetailResponseDto responseDto = new MemberDetailResponseDto(
                1L, "admin123", Role.ROLE_HEADQUARTERS,
                "홍길동", "admin@kb.com", "010-1234-5678"
        );

        given(memberService.register(any(MemberSignUpRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("admin123"))
                .andDo(document("member-signup",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("WMS 사원(회원) 등록")
                                .description("새로운 사원을 시스템에 등록합니다."),
                        getSignupRequestFields(),
                        getMemberDetailResponseFields()
                ));
    }

    @Test
    void 회원가입_실패_아이디가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest()
                .loginId(" ")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-blank-id",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("회원 가입 실패 - 아이디 공백")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "  ", "가나다"})
    void 회원가입_실패_비밀번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidPwd) throws Exception {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest()
                .password(invalidPwd)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-password/{index}",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("회원 가입 실패 - 비밀번호 형식 오류")
                ));
    }

    @Test
    void 회원가입_실패_회원명이_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest()
                .memberName(" ")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-blank-memberName",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("회원 가입 실패 - 회원명 공백")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"adsfr", " ", "adsfwe@"})
    void 회원가입_실패_이메일_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다(String invalidEmail) throws Exception {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest()
                .email(invalidEmail)
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-email/{index}",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("회원 가입 실패 - 이메일 형식 오류")
                ));
    }

    @Test
    void 회원가입_실패_전화번호_형식이_올바르지_않으면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        MemberSignUpRequestDto requestDto = MemberDtoBuilder.signUpRequest()
                .phoneNumber("01012345678")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-signup-fail-invalid-phoneNumber",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("회원 가입 실패 - 전화번호 형식 오류")
                ));
    }

    @Test
    void 올바른_로그인_아이디와_비밀번호를_입력하면_200_OK와_함께_회원_정보를_반환하고_API_문서가_생성된다() throws Exception {
        // given
        MemberLoginRequestDto requestDto = MemberDtoBuilder.loginRequest().build();
        MemberDetailResponseDto responseDto = new MemberDetailResponseDto(
                1L, "admin01", Role.ROLE_HEADQUARTERS, "홍길동", "admin@example.com", "010-1234-5678"
        );

        given(memberService.login(any(MemberLoginRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loginId").value("admin01"))
                .andExpect(jsonPath("$.role").value("ROLE_HEADQUARTERS"))
                .andDo(document("member-login-success",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("WMS 사원 로그인")
                                .description("아이디와 비밀번호로 로그인을 수행합니다."),
                        getLoginRequestFields(),
                        getMemberDetailResponseFields()
                ));
    }

    @Test
    void 비밀번호가_공백이면_400_BAD_REQUEST를_반환한다() throws Exception {
        // given
        MemberLoginRequestDto requestDto = MemberDtoBuilder.loginRequest()
                .password("")
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andDo(document("member-login-fail-blank-password",
                        ResourceSnippetParameters.builder()
                                .tag("Member API")
                                .summary("로그인 실패 - 비밀번호 공백")
                ));
    }
}
