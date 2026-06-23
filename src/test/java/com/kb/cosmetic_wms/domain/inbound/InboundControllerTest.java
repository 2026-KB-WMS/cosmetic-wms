package com.kb.cosmetic_wms.domain.inbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.inbound.controller.InboundController;
import com.kb.cosmetic_wms.domain.inbound.dto.*;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundErrorCode;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundItemNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.exception.InboundProductNotFoundException;
import com.kb.cosmetic_wms.domain.inbound.fixture.InboundDtoBuilder;
import com.kb.cosmetic_wms.domain.inbound.service.InboundService;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerErrorCode;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
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
    private InboundService inboundService;

    // --- 입고 전표 등록 ---

    @Nested
    class 입고_전표_등록 {

        @Test
        @WithMockUser
        void 올바른_정보로_입고_전표를_등록하면_201_Created와_API_문서가_생성된다() throws Exception {
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().buildCreateRequest();
            InboundDetailResponseDto response = buildScheduledInboundResponse();
            given(inboundService.registerInbound(any(InboundCreateRequestDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.warehouseId").value(1L))
                    .andExpect(jsonPath("$.partnerId").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("SCHEDULED"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andDo(document("inbound-register-success",
                            buildParams(INBOUND, "입고 전표 등록", INBOUND_CREATE_REQUEST, INBOUND_DETAIL_RESPONSE),
                            createRequestFields(getInboundCreateRequestFields()),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 창고_ID가_null이면_400_Bad_Request를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            InboundCreateRequestDto invalidRequest = new InboundDtoBuilder().warehouseId(null).buildCreateRequest();

            // when & then
            mockMvc.perform(post("/api/v1/inbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andDo(document("inbound-register-fail-null-warehouse",
                            buildErrorParams(INBOUND, "입고 전표 등록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 파트너_ID가_null이면_400_Bad_Request를_반환한다() throws Exception {
            // given
            InboundCreateRequestDto invalidRequest = new InboundDtoBuilder().partnerId(null).buildCreateRequest();

            // when & then
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
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().buildCreateRequest();
            given(inboundService.registerInbound(any(InboundCreateRequestDto.class)))
                    .willThrow(new WarehouseNotFoundException());

            // when & then
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
            // given
            InboundCreateRequestDto request = new InboundDtoBuilder().buildCreateRequest();
            given(inboundService.registerInbound(any(InboundCreateRequestDto.class)))
                    .willThrow(new PartnerNotFoundException());

            // when & then
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
            // given
            InboundDetailResponseDto response = buildScheduledInboundResponse();
            given(inboundService.getInbound(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/inbounds/{inboundId}", 1L)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("SCHEDULED"))
                    .andExpect(jsonPath("$.inboundDate").exists())
                    .andDo(document("inbound-get-success",
                            buildParams(INBOUND, "입고 전표 조회", null, INBOUND_DETAIL_RESPONSE),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_조회하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            given(inboundService.getInbound(999L)).willThrow(new InboundNotFoundException());

            // when & then
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

    // --- 품목 추가 ---

    @Nested
    class 품목_추가 {

        @Test
        @WithMockUser
        void 올바른_품목_정보를_추가하면_200_OK와_갱신된_입고_전표를_반환하고_API_문서가_생성된다() throws Exception {
            // given
            InboundItemAddRequestDto request = new InboundDtoBuilder().buildAddItemRequest();
            InboundDetailResponseDto response = buildInboundResponseWithItem();
            given(inboundService.addItem(eq(1L), any(InboundItemAddRequestDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/inbounds/{inboundId}/items", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("SCHEDULED"))
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").value(1L))
                    .andExpect(jsonPath("$.items[0].inspectionStatus").value("WAITING"))
                    .andDo(document("inbound-add-item-success",
                            buildParams(INBOUND, "입고 품목 추가", INBOUND_ITEM_ADD_REQUEST, INBOUND_DETAIL_RESPONSE),
                            createRequestFields(getInboundItemAddRequestFields()),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 상품_ID가_null이면_400_Bad_Request를_반환한다() throws Exception {
            // given
            InboundItemAddRequestDto invalidRequest = new InboundDtoBuilder().productId(null).buildAddItemRequest();

            // when & then
            mockMvc.perform(post("/api/v1/inbounds/{inboundId}/items", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 수량이_0이면_400_Bad_Request를_반환한다() throws Exception {
            // given
            InboundItemAddRequestDto invalidRequest = new InboundDtoBuilder().quantity(0).buildAddItemRequest();

            // when & then
            mockMvc.perform(post("/api/v1/inbounds/{inboundId}/items", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID에_품목을_추가하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            InboundItemAddRequestDto request = new InboundDtoBuilder().buildAddItemRequest();
            given(inboundService.addItem(eq(999L), any(InboundItemAddRequestDto.class)))
                    .willThrow(new InboundNotFoundException());

            // when & then
            mockMvc.perform(post("/api/v1/inbounds/{inboundId}/items", 999L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-add-item-fail-inbound-not-found",
                            buildErrorParams(INBOUND, "입고 품목 추가"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_상품_ID로_품목을_추가하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            InboundItemAddRequestDto request = new InboundDtoBuilder().buildAddItemRequest();
            given(inboundService.addItem(eq(1L), any(InboundItemAddRequestDto.class)))
                    .willThrow(new InboundProductNotFoundException());

            // when & then
            mockMvc.perform(post("/api/v1/inbounds/{inboundId}/items", 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_PRODUCT_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-add-item-fail-product-not-found",
                            buildErrorParams(INBOUND, "입고 품목 추가"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 입고 작업 시작 ---

    @Nested
    class 입고_작업_시작 {

        @Test
        @WithMockUser
        void 입고_작업을_시작하면_200_OK와_IN_PROGRESS_상태의_전표가_반환되고_API_문서가_생성된다() throws Exception {
            // given
            InboundDetailResponseDto response = buildInboundResponse(InboundStatus.IN_PROGRESS);
            given(inboundService.startInbound(1L)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/start", 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("IN_PROGRESS"))
                    .andDo(document("inbound-start-success",
                            buildParams(INBOUND, "입고 작업 시작", null, INBOUND_DETAIL_RESPONSE),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_작업_시작을_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            given(inboundService.startInbound(999L)).willThrow(new InboundNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/start", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-start-fail-not-found",
                            buildErrorParams(INBOUND, "입고 작업 시작"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 입고 완료 ---

    @Nested
    class 입고_완료 {

        @Test
        @WithMockUser
        void 입고를_완료하면_200_OK와_COMPLETED_상태의_전표가_반환되고_API_문서가_생성된다() throws Exception {
            // given
            InboundDetailResponseDto response = buildInboundResponse(InboundStatus.COMPLETED);
            given(inboundService.completeInbound(1L)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/complete", 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inboundStatus").value("COMPLETED"))
                    .andDo(document("inbound-complete-success",
                            buildParams(INBOUND, "입고 완료", null, INBOUND_DETAIL_RESPONSE),
                            createResponseFields(getInboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_입고_ID로_완료를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            given(inboundService.completeInbound(999L)).willThrow(new InboundNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/complete", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-complete-fail-not-found",
                            buildErrorParams(INBOUND, "입고 완료"),
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
            // given
            InboundDetailResponseDto response = buildInboundResponse(InboundStatus.CANCELED);
            given(inboundService.cancelInbound(1L)).willReturn(response);

            // when & then
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
            // given
            given(inboundService.cancelInbound(999L)).willThrow(new InboundNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/cancel", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-cancel-fail-not-found",
                            buildErrorParams(INBOUND, "입고 취소"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 실물 적재 완료 ---

    @Nested
    class 실물_적재_완료 {

        @Test
        @WithMockUser
        void 적재를_완료하면_200_OK와_INSPECTING_상태의_품목이_반환되고_API_문서가_생성된다() throws Exception {
            // given
            InboundPutawayRequestDto request = new InboundDtoBuilder().buildPutawayRequest();
            InboundItemResponseDto response = buildItemResponse(InspectionStatus.INSPECTING, 100L, 200L);
            given(inboundService.completePutaway(eq(1L), eq(1L), any(InboundPutawayRequestDto.class))).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/putaway", 1L, 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inspectionStatus").value("INSPECTING"))
                    .andExpect(jsonPath("$.lotId").value(100L))
                    .andExpect(jsonPath("$.sectionId").value(200L))
                    .andDo(document("inbound-item-putaway-success",
                            buildParams(INBOUND, "실물 적재 완료", INBOUND_PUTAWAY_REQUEST, INBOUND_ITEM_RESPONSE),
                            createRequestFields(getInboundPutawayRequestFields()),
                            createResponseFields(getInboundItemResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 로트_ID가_null이면_400_Bad_Request를_반환한다() throws Exception {
            // given — lotId 없이 전송
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/putaway", 1L, 1L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sectionId\": 200}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_품목_ID로_적재를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            InboundPutawayRequestDto request = new InboundDtoBuilder().buildPutawayRequest();
            given(inboundService.completePutaway(eq(1L), eq(999L), any(InboundPutawayRequestDto.class)))
                    .willThrow(new InboundItemNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/putaway", 1L, 999L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_ITEM_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-item-putaway-fail-not-found",
                            buildErrorParams(INBOUND, "실물 적재 완료"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 검수 정상 완료 ---

    @Nested
    class 검수_정상_완료 {

        @Test
        @WithMockUser
        void 검수를_정상_완료하면_200_OK와_NORMAL_상태의_품목이_반환되고_API_문서가_생성된다() throws Exception {
            // given
            InboundItemResponseDto response = buildItemResponse(InspectionStatus.NORMAL, 100L, 200L);
            given(inboundService.approveItem(1L, 1L)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/approve", 1L, 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inspectionStatus").value("NORMAL"))
                    .andDo(document("inbound-item-approve-success",
                            buildParams(INBOUND, "검수 정상 완료", null, INBOUND_ITEM_RESPONSE),
                            createResponseFields(getInboundItemResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_품목_ID로_검수_정상_처리를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            given(inboundService.approveItem(1L, 999L)).willThrow(new InboundItemNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/approve", 1L, 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_ITEM_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-item-approve-fail-not-found",
                            buildErrorParams(INBOUND, "검수 정상 완료"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 검수 보류 ---

    @Nested
    class 검수_보류 {

        @Test
        @WithMockUser
        void 검수를_보류_처리하면_200_OK와_HOLD_상태의_품목이_반환되고_API_문서가_생성된다() throws Exception {
            // given
            InboundItemResponseDto response = buildItemResponse(InspectionStatus.HOLD, 100L, 200L);
            given(inboundService.holdItem(1L, 1L)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/hold", 1L, 1L)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.inspectionStatus").value("HOLD"))
                    .andDo(document("inbound-item-hold-success",
                            buildParams(INBOUND, "검수 보류", null, INBOUND_ITEM_RESPONSE),
                            createResponseFields(getInboundItemResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_품목_ID로_검수_보류를_요청하면_404_Not_Found를_반환하고_에러응답이_문서화된다() throws Exception {
            // given
            given(inboundService.holdItem(1L, 999L)).willThrow(new InboundItemNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/inbounds/{inboundId}/items/{itemId}/hold", 1L, 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("INBOUND_ITEM_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage()))
                    .andDo(document("inbound-item-hold-fail-not-found",
                            buildErrorParams(INBOUND, "검수 보류"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // --- 응답 픽스처 ---

    private static InboundDetailResponseDto buildScheduledInboundResponse() {
        return buildInboundResponse(InboundStatus.SCHEDULED);
    }

    private static InboundDetailResponseDto buildInboundResponse(InboundStatus status) {
        return new InboundDetailResponseDto(
                1L, 1L, 1L, status,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                List.of()
        );
    }

    private static InboundDetailResponseDto buildInboundResponseWithItem() {
        InboundItemResponseDto item = new InboundItemResponseDto(
                1L, 1L, 100,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 1, 1),
                InspectionStatus.WAITING, null, null
        );
        return new InboundDetailResponseDto(
                1L, 1L, 1L, InboundStatus.SCHEDULED,
                LocalDateTime.of(2026, 7, 1, 10, 0),
                List.of(item)
        );
    }

    private static InboundItemResponseDto buildItemResponse(
            InspectionStatus status, Long lotId, Long sectionId) {
        return new InboundItemResponseDto(
                1L, 1L, 100,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2028, 1, 1),
                status, lotId, sectionId
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
                        .description("입고 예정일 (ISO-8601, 오늘 이후여야 함)")
        };
    }

    private static FieldDescriptor[] getInboundItemAddRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("productId").type(JsonFieldType.NUMBER)
                        .description("입고 상품 ID (필수)"),
                fieldWithPath("quantity").type(JsonFieldType.NUMBER)
                        .description("입고 예정 수량 (1 이상)"),
                fieldWithPath("manufactureDate").type(JsonFieldType.STRING)
                        .description("제조일자 (ISO-8601)"),
                fieldWithPath("expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한 (ISO-8601)")
        };
    }

    private static FieldDescriptor[] getInboundPutawayRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("lotId").type(JsonFieldType.NUMBER)
                        .description("적재 완료 후 발행된 로트 ID (필수)"),
                fieldWithPath("sectionId").type(JsonFieldType.NUMBER)
                        .description("상품이 적재된 섹션 ID (필수)")
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
                        .description("입고 상태 (SCHEDULED / IN_PROGRESS / COMPLETED / CANCELED)"),
                fieldWithPath("inboundDate").type(JsonFieldType.STRING)
                        .description("입고 예정일"),
                fieldWithPath("items").type(JsonFieldType.ARRAY)
                        .description("입고 품목 목록"),
                fieldWithPath("items[].id").type(JsonFieldType.NUMBER)
                        .description("품목 ID").optional(),
                fieldWithPath("items[].productId").type(JsonFieldType.NUMBER)
                        .description("상품 ID").optional(),
                fieldWithPath("items[].quantity").type(JsonFieldType.NUMBER)
                        .description("수량").optional(),
                fieldWithPath("items[].manufactureDate").type(JsonFieldType.STRING)
                        .description("제조일자").optional(),
                fieldWithPath("items[].expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한").optional(),
                fieldWithPath("items[].inspectionStatus").type(JsonFieldType.STRING)
                        .description("검수 상태 (WAITING / INSPECTING / NORMAL / HOLD)").optional(),
                fieldWithPath("items[].lotId").type(JsonFieldType.VARIES)
                        .description("로트 ID (적재 전 null)").optional(),
                fieldWithPath("items[].sectionId").type(JsonFieldType.VARIES)
                        .description("섹션 ID (적재 전 null)").optional()
        };
    }

    private static FieldDescriptor[] getInboundItemResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").type(JsonFieldType.NUMBER)
                        .description("품목 ID"),
                fieldWithPath("productId").type(JsonFieldType.NUMBER)
                        .description("상품 ID"),
                fieldWithPath("quantity").type(JsonFieldType.NUMBER)
                        .description("수량"),
                fieldWithPath("manufactureDate").type(JsonFieldType.STRING)
                        .description("제조일자"),
                fieldWithPath("expirationDate").type(JsonFieldType.STRING)
                        .description("유통기한"),
                fieldWithPath("inspectionStatus").type(JsonFieldType.STRING)
                        .description("검수 상태 (WAITING / INSPECTING / NORMAL / HOLD)"),
                fieldWithPath("lotId").type(JsonFieldType.VARIES)
                        .description("로트 ID (적재 전 null)").optional(),
                fieldWithPath("sectionId").type(JsonFieldType.VARIES)
                        .description("섹션 ID (적재 전 null)").optional()
        };
    }
}
