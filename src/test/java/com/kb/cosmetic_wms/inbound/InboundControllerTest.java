package com.kb.cosmetic_wms.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundController;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundCreateRequest;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundReceiveRequest;
import com.kb.cosmetic_wms.inbound.application.port.in.*;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundErrorCode;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.inbound.fixture.InboundRequestBuilder;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.storage.domain.exception.StorageErrorCode;
import com.kb.cosmetic_wms.storage.domain.exception.WarehouseNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.INBOUND;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {InboundController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class InboundControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InboundLifecycleUseCase inboundLifecycleUseCase;

    @MockitoBean
    private FindInboundUseCase findInboundUseCase;

    // --- 입고 전표 등록 ---

    @Nested
    class 입고_전표_등록 {

        @Test
        @WithMockUser
        void 올바른_정보로_입고_전표를_등록하면_201_Created와_API_문서가_생성된다() throws Exception {
            InboundCreateRequest request = new InboundRequestBuilder().buildCreateRequest();
            given(inboundLifecycleUseCase.register(any(RegisterInboundCommand.class)))
                    .willReturn(buildScheduledResult());

            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("SCHEDULED"))
                    .andExpect(jsonPath("$.lines").isArray())
                    .andDo(document("inbound-register-success",
                            buildParams(INBOUND, "입고 전표 등록", INBOUND_CREATE_REQUEST, INBOUND_DETAIL_RESPONSE),
                            createRequestFields(getInboundCreateRequestFields()),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 창고_ID가_null이면_400_Bad_Request를_반환한다() throws Exception {
            InboundCreateRequest invalidRequest = new InboundRequestBuilder().warehouseId(null).buildCreateRequest();

            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 파트너_ID가_null이면_400_Bad_Request를_반환한다() throws Exception {
            InboundCreateRequest invalidRequest = new InboundRequestBuilder().partnerId(null).buildCreateRequest();

            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_창고_ID로_등록하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            InboundCreateRequest request = new InboundRequestBuilder().buildCreateRequest();
            given(inboundLifecycleUseCase.register(any(RegisterInboundCommand.class)))
                    .willThrow(new WarehouseNotFoundException());

            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("STORAGE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(StorageErrorCode.STORAGE_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-register-fail-warehouse-not-found",
                            buildErrorParams(INBOUND, "입고 전표 등록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_파트너_ID로_등록하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            InboundCreateRequest request = new InboundRequestBuilder().buildCreateRequest();
            given(inboundLifecycleUseCase.register(any(RegisterInboundCommand.class)))
                    .willThrow(new PartnerNotFoundException());

            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("PARTNER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(PartnerErrorCode.PARTNER_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-register-fail-partner-not-found",
                            buildErrorParams(INBOUND, "입고 전표 등록"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 입고 전표 조회 ---

    @Nested
    class 입고_전표_조회 {

        @Test
        @WithMockUser
        void 존재하는_입고_ID로_조회하면_200_OK와_상세정보를_반환하고_API_문서가_생성된다() throws Exception {
            given(findInboundUseCase.findById(1L)).willReturn(buildScheduledResult());

            mockMvc.perform(get("/api/v1/inbounds/{inboundId}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("SCHEDULED"))
                    .andDo(document("inbound-get-success",
                            buildParams(INBOUND, "입고 전표 조회", null, INBOUND_DETAIL_RESPONSE),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_조회하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            given(findInboundUseCase.findById(999L)).willThrow(new InboundNotFoundException());

            mockMvc.perform(get("/api/v1/inbounds/{inboundId}", 999L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-get-fail-not-found",
                            buildErrorParams(INBOUND, "입고 전표 조회"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 수령 확인 ---

    @Nested
    class 수령_확인 {

        @Test
        @WithMockUser
        void 수령_확인을_요청하면_200_OK와_RECEIVED_상태의_전표가_반환되고_API_문서가_생성된다() throws Exception {
            InboundReceiveRequest request = new InboundRequestBuilder().buildReceiveRequest(1L);
            given(inboundLifecycleUseCase.receive(eq(1L), any(ReceiveInboundCommand.class)))
                    .willReturn(buildResult(InboundStatus.RECEIVED, 100, 100));

            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/receive", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("RECEIVED"))
                    .andExpect(jsonPath("$.lines[0].receivedQuantity").value(100))
                    .andDo(document("inbound-receive-success",
                            buildParams(INBOUND, "수령 확인", INBOUND_RECEIVE_REQUEST, INBOUND_DETAIL_RESPONSE),
                            createRequestFields(getInboundReceiveRequestFields()),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 라인_목록이_비어있으면_400_Bad_Request를_반환한다() throws Exception {
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/receive", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"lines\": []}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_수령_확인을_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            InboundReceiveRequest request = new InboundRequestBuilder().buildReceiveRequest(1L);
            given(inboundLifecycleUseCase.receive(eq(999L), any(ReceiveInboundCommand.class)))
                    .willThrow(new InboundNotFoundException());

            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/receive", 999L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andDo(document("inbound-receive-fail-not-found",
                            buildErrorParams(INBOUND, "수령 확인"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 입고 취소 ---

    @Nested
    class 입고_취소 {

        @Test
        @WithMockUser
        void 입고를_취소하면_200_OK와_CANCELED_상태의_전표가_반환되고_API_문서가_생성된다() throws Exception {
            given(inboundLifecycleUseCase.cancel(1L)).willReturn(buildResult(InboundStatus.CANCELED, 100, 0));

            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/cancel", 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("CANCELED"))
                    .andDo(document("inbound-cancel-success",
                            buildParams(INBOUND, "입고 취소", null, INBOUND_DETAIL_RESPONSE),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_취소를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            given(inboundLifecycleUseCase.cancel(999L)).willThrow(new InboundNotFoundException());

            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/cancel", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andDo(document("inbound-cancel-fail-not-found",
                            buildErrorParams(INBOUND, "입고 취소"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 응답 픽스처 ---

    private static InboundResult buildScheduledResult() {
        return buildResult(InboundStatus.SCHEDULED, 100, 0);
    }

    private static InboundResult buildResult(InboundStatus status, int orderedQty, int receivedQty) {
        InboundLineResult line = new InboundLineResult(
                1L, 1L, orderedQty, receivedQty,
                LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1)
        );
        return new InboundResult(
                1L, 1L, 1L, status,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                List.of(line)
        );
    }

    // --- 요청 필드 디스크립터 ---

    private static FieldDescriptor[] getInboundCreateRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("warehouseId").type(JsonFieldType.NUMBER)
                        .description("입고할 창고 ID (필수)"),
                fieldWithPath("partnerId").type(JsonFieldType.NUMBER)
                        .description("협력사 ID (필수)"),
                fieldWithPath("inboundDate").type(JsonFieldType.STRING)
                        .description("입고 예정일 (ISO-8601, 오늘 이후여야 함)"),
                fieldWithPath("lines").type(JsonFieldType.ARRAY)
                        .description("입고 품목 라인 목록 (1개 이상 필수)"),
                fieldWithPath("lines[].productId").type(JsonFieldType.NUMBER)
                        .description("상품 ID"),
                fieldWithPath("lines[].orderedQuantity").type(JsonFieldType.NUMBER)
                        .description("발주 수량 (1 이상)"),
                fieldWithPath("lines[].manufactureDate").type(JsonFieldType.STRING)
                        .description("제조일자 (ISO-8601)"),
                fieldWithPath("lines[].expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한 (ISO-8601)")
        };
    }

    private static FieldDescriptor[] getInboundReceiveRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("lines").type(JsonFieldType.ARRAY)
                        .description("수령 확인 라인 목록 (1개 이상 필수)"),
                fieldWithPath("lines[].lineId").type(JsonFieldType.NUMBER)
                        .description("입고 라인 ID"),
                fieldWithPath("lines[].receivedQuantity").type(JsonFieldType.NUMBER)
                        .description("실제 수령 수량 (0 이상)")
        };
    }

    // --- 응답 필드 디스크립터 ---

    private static FieldDescriptor[] getInboundDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER)
                        .description("입고 전표 ID"),
                fieldWithPath("warehouseId").type(JsonFieldType.NUMBER)
                        .description("창고 ID"),
                fieldWithPath("partnerId").type(JsonFieldType.NUMBER)
                        .description("협력사 ID"),
                fieldWithPath("inboundStatus").type(JsonFieldType.STRING)
                        .description("입고 상태 (SCHEDULED / RECEIVED / CANCELED)"),
                fieldWithPath("inboundDate").type(JsonFieldType.STRING)
                        .description("입고 예정일"),
                fieldWithPath("lines").type(JsonFieldType.ARRAY)
                        .description("입고 품목 라인 목록"),
                fieldWithPath("lines[].id").type(JsonFieldType.NUMBER)
                        .description("라인 ID").optional(),
                fieldWithPath("lines[].productId").type(JsonFieldType.NUMBER)
                        .description("상품 ID").optional(),
                fieldWithPath("lines[].orderedQuantity").type(JsonFieldType.NUMBER)
                        .description("발주 수량").optional(),
                fieldWithPath("lines[].receivedQuantity").type(JsonFieldType.NUMBER)
                        .description("수령 수량 (수령 전 0)").optional(),
                fieldWithPath("lines[].manufactureDate").type(JsonFieldType.STRING)
                        .description("제조일자").optional(),
                fieldWithPath("lines[].expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한").optional()
        };
    }
}
