package com.kb.auth.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.auth.global.config.SecurityConfig;
import com.kb.auth.global.restdocs.RestDocsSupport;
import com.kb.auth.member.adapter.in.web.MemberController;
import com.kb.auth.member.adapter.in.web.dto.ChangeRoleRequest;
import com.kb.auth.member.application.port.in.ChangeRoleUseCase;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.domain.exception.MemberNotFoundException;
import com.kb.auth.member.domain.model.Role;
import com.kb.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.kb.auth.global.restdocs.ApiDocs.MEMBER;
import static com.kb.auth.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MemberController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class MemberControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FindMemberUseCase findMemberUseCase;

    @MockitoBean
    private ChangeRoleUseCase changeRoleUseCase;

    @Nested
    class 회원_목록_조회 {

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void 본사_관리자는_200_OK와_페이지_목록을_반환한다() throws Exception {
            // given
            List<MemberResult> members = List.of(memberResult(1L, Role.ROLE_WAREHOUSE_MANAGER));
            given(findMemberUseCase.findAll(any())).willReturn(
                    new PageImpl<>(members, PageRequest.of(0, 20), 1)
            );

            // when & then
            mockMvc.perform(get("/api/v1/members"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].memberId").value(1L))
                    .andExpect(jsonPath("$.content[0].memberName").value("홍길동"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andDo(document("member-list-success",
                            buildParams(MEMBER, "회원 목록 조회", null, MEMBER_PAGE_RESPONSE),
                            createResponseFields(
                                    fieldWithPath("content[].memberId").type(NUMBER).description("회원 ID"),
                                    fieldWithPath("content[].memberName").type(STRING).description("회원 이름"),
                                    fieldWithPath("content[].email").type(STRING).description("이메일 주소"),
                                    fieldWithPath("content[].phoneNumber").type(STRING).description("전화번호"),
                                    fieldWithPath("content[].role").type(STRING).description("회원 역할"),
                                    fieldWithPath("page").type(NUMBER).description("현재 페이지 번호 (0부터 시작)"),
                                    fieldWithPath("size").type(NUMBER).description("페이지 크기"),
                                    fieldWithPath("totalElements").type(NUMBER).description("전체 회원 수"),
                                    fieldWithPath("totalPages").type(NUMBER).description("전체 페이지 수")
                            )
                    ));
        }

        @Test
        @WithMockUser
        void HEADQUARTERS_역할이_없으면_403_FORBIDDEN을_반환한다() throws Exception {
            mockMvc.perform(get("/api/v1/members"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class 회원_단건_조회 {

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void 존재하는_회원_ID로_조회하면_200_OK와_MemberResponse를_반환한다() throws Exception {
            // given
            given(findMemberUseCase.findById(1L)).willReturn(memberResult(1L, Role.ROLE_WAREHOUSE_MANAGER));

            // when & then
            mockMvc.perform(get("/api/v1/members/{id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1L))
                    .andExpect(jsonPath("$.memberName").value("홍길동"))
                    .andDo(document("member-get-success",
                            buildParams(MEMBER, "회원 단건 조회", null, MEMBER_RESPONSE)
                                    .pathParameters(parameterWithName("id").description("회원 ID")),
                            createResponseFields(
                                    fieldWithPath("memberId").type(NUMBER).description("회원 ID"),
                                    fieldWithPath("memberName").type(STRING).description("회원 이름"),
                                    fieldWithPath("email").type(STRING).description("이메일 주소"),
                                    fieldWithPath("phoneNumber").type(STRING).description("전화번호"),
                                    fieldWithPath("role").type(STRING).description("회원 역할")
                            )
                    ));
        }

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void 존재하지_않는_회원_ID로_조회하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            given(findMemberUseCase.findById(999L)).willThrow(new MemberNotFoundException());

            // when & then
            mockMvc.perform(get("/api/v1/members/{id}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"))
                    .andDo(document("member-get-fail-not-found",
                            buildErrorParams(MEMBER, "회원 단건 조회")
                                    .pathParameters(parameterWithName("id").description("회원 ID")),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 역할_변경 {

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void 올바른_역할로_변경하면_200_OK와_변경된_MemberResponse를_반환한다() throws Exception {
            // given
            ChangeRoleRequest request = new ChangeRoleRequest(Role.ROLE_FRANCHISE_MANAGER);
            given(changeRoleUseCase.changeRole(any())).willReturn(memberResult(1L, Role.ROLE_FRANCHISE_MANAGER));

            // when & then
            mockMvc.perform(patch("/api/v1/members/{id}/role", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.memberId").value(1L))
                    .andExpect(jsonPath("$.role").value("ROLE_FRANCHISE_MANAGER"))
                    .andDo(document("member-change-role-success",
                            buildParams(MEMBER, "회원 역할 변경", CHANGE_ROLE_REQUEST, MEMBER_RESPONSE)
                                    .pathParameters(parameterWithName("id").description("회원 ID")),
                            createRequestFields(
                                    fieldWithPath("role").type(STRING).description("변경할 역할 (ROLE_HEADQUARTERS / ROLE_WAREHOUSE_MANAGER / ROLE_FRANCHISE_MANAGER)")
                            ),
                            createResponseFields(
                                    fieldWithPath("memberId").type(NUMBER).description("회원 ID"),
                                    fieldWithPath("memberName").type(STRING).description("회원 이름"),
                                    fieldWithPath("email").type(STRING).description("이메일 주소"),
                                    fieldWithPath("phoneNumber").type(STRING).description("전화번호"),
                                    fieldWithPath("role").type(STRING).description("변경된 회원 역할")
                            )
                    ));
        }

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void role이_null이면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            ChangeRoleRequest request = new ChangeRoleRequest(null);

            // when & then
            mockMvc.perform(patch("/api/v1/members/{id}/role", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("member-change-role-fail-null-role",
                            buildErrorParams(MEMBER, "회원 역할 변경")
                                    .pathParameters(parameterWithName("id").description("회원 ID")),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser(roles = "HEADQUARTERS")
        void 존재하지_않는_회원의_역할을_변경하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            ChangeRoleRequest request = new ChangeRoleRequest(Role.ROLE_FRANCHISE_MANAGER);
            willThrow(new MemberNotFoundException()).given(changeRoleUseCase).changeRole(any());

            // when & then
            mockMvc.perform(patch("/api/v1/members/{id}/role", 999L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("MEMBER_NOT_FOUND"))
                    .andDo(document("member-change-role-fail-not-found",
                            buildErrorParams(MEMBER, "회원 역할 변경")
                                    .pathParameters(parameterWithName("id").description("회원 ID")),
                            globalErrorResponseFields()
                    ));
        }
    }

    // ── Fixtures ──

    private static MemberResult memberResult(Long memberId, Role role) {
        return new MemberResult(memberId, "홍길동", "test@example.com", "010-1234-5678", role);
    }
}
