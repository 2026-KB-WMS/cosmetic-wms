package com.kb.cosmetic_wms.domain.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.domain.outbound.controller.OutboundController;
import com.kb.cosmetic_wms.domain.outbound.dto.CreateOutboundRequestDto;
import com.kb.cosmetic_wms.domain.outbound.dto.OutboundResponseDto;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.exception.*;
import com.kb.cosmetic_wms.domain.outbound.service.OutboundService;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.OUTBOUND;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.OUTBOUND_CREATE_REQUEST;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.OUTBOUND_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OutboundController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class OutboundControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OutboundService outboundService;

    @Nested
    class 출고_의뢰_등록 {

        @Test
        @WithMockUser
        void 올바른_출고_정보가_주어지면_201_CREATED와_PENDING_상태의_전표를_반환한다() throws Exception {
            // given
            CreateOutboundRequestDto request = createRequest();
            given(outboundService.createOutbound(any(), any(), any(), any()))
                    .willReturn(outboundResponse(1L, OutboundStatus.PENDING));

            // when & then
            mockMvc.perform(post("/api/v1/outbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.outboundId").value(1L))
                    .andExpect(jsonPath("$.outboundType").value("ORDER"))
                    .andExpect(jsonPath("$.outboundStatus").value("PENDING"))
                    .andDo(document("outbound-create-success",
                            buildParams(OUTBOUND, "출고 의뢰 등록", OUTBOUND_CREATE_REQUEST, OUTBOUND_RESPONSE),
                            createRequestFields(getCreateOutboundRequestFields()),
                            createResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 발주_ID가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            CreateOutboundRequestDto request = new CreateOutboundRequestDto(
                    null, 10L, OutboundType.ORDER,
                    List.of(new CreateOutboundRequestDto.OutboundItemRequestDto(1L, 1L, 5))
            );

            // when & then
            mockMvc.perform(post("/api/v1/outbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("outbound-create-fail-no-orders-id",
                            buildErrorParams(OUTBOUND, "출고 의뢰 등록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 창고_ID가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            CreateOutboundRequestDto request = new CreateOutboundRequestDto(
                    1L, null, OutboundType.ORDER,
                    List.of(new CreateOutboundRequestDto.OutboundItemRequestDto(1L, 1L, 5))
            );

            // when & then
            mockMvc.perform(post("/api/v1/outbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("outbound-create-fail-no-warehouse",
                            buildErrorParams(OUTBOUND, "출고 의뢰 등록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 출고_유형이_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            CreateOutboundRequestDto request = new CreateOutboundRequestDto(
                    1L, 10L, null,
                    List.of(new CreateOutboundRequestDto.OutboundItemRequestDto(1L, 1L, 5))
            );

            // when & then
            mockMvc.perform(post("/api/v1/outbounds")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("outbound-create-fail-no-type",
                            buildErrorParams(OUTBOUND, "출고 의뢰 등록"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 창고_재고_할당 {

        @Test
        @WithMockUser
        void 출고_대기_상태의_전표에_재고_할당을_요청하면_200_OK와_ALLOCATED_상태를_반환한다() throws Exception {
            // given
            Long outboundId = 1L;
            given(outboundService.allocateInventory(outboundId))
                    .willReturn(outboundResponse(outboundId, OutboundStatus.ALLOCATED));

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/allocate", outboundId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outboundStatus").value("ALLOCATED"))
                    .andDo(document("outbound-allocate-success",
                            buildParams(OUTBOUND, "재고 할당", null, OUTBOUND_RESPONSE),
                            createResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 이미_할당되었거나_준비_중인_전표에_재고_할당을_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(outboundService.allocateInventory(anyLong()))
                    .willThrow(new OutboundAllocateNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/allocate", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_ALLOCATE_NOT_ALLOWED"))
                    .andDo(document("outbound-allocate-fail-not-allowed",
                            buildErrorParams(OUTBOUND, "재고 할당"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_출고_ID로_재고_할당을_요청하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            given(outboundService.allocateInventory(anyLong()))
                    .willThrow(new OutboundNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/allocate", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_NOT_FOUND"))
                    .andDo(document("outbound-allocate-fail-not-found",
                            buildErrorParams(OUTBOUND, "재고 할당"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 출고_작업_준비 {

        @Test
        @WithMockUser
        void 재고_할당_완료된_전표에_출고_작업_준비를_요청하면_200_OK와_PROCESSING_상태를_반환한다() throws Exception {
            // given
            Long outboundId = 1L;
            given(outboundService.startProcessing(outboundId))
                    .willReturn(outboundResponse(outboundId, OutboundStatus.PROCESSING));

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/process", outboundId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outboundStatus").value("PROCESSING"))
                    .andDo(document("outbound-process-success",
                            buildParams(OUTBOUND, "출고 작업 준비", null, OUTBOUND_RESPONSE),
                            createResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 재고_할당_이외의_상태에서_출고_작업_준비를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(outboundService.startProcessing(anyLong()))
                    .willThrow(new OutboundProcessingNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/process", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_PROCESSING_NOT_ALLOWED"))
                    .andDo(document("outbound-process-fail-not-allowed",
                            buildErrorParams(OUTBOUND, "출고 작업 준비"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 출하_완료 {

        @Test
        @WithMockUser
        void 출고_준비_중_상태의_전표에_출하를_요청하면_200_OK와_SHIPPED_상태를_반환한다() throws Exception {
            // given
            Long outboundId = 1L;
            given(outboundService.ship(outboundId))
                    .willReturn(outboundResponse(outboundId, OutboundStatus.SHIPPED));

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/ship", outboundId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outboundStatus").value("SHIPPED"))
                    .andDo(document("outbound-ship-success",
                            buildParams(OUTBOUND, "출하 완료", null, OUTBOUND_RESPONSE),
                            createResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 출고_준비_중_이외의_상태에서_출하를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(outboundService.ship(anyLong()))
                    .willThrow(new OutboundShipNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/ship", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_SHIP_NOT_ALLOWED"))
                    .andDo(document("outbound-ship-fail-not-allowed",
                            buildErrorParams(OUTBOUND, "출하 완료"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 출고_취소 {

        @Test
        @WithMockUser
        void 출고_대기_또는_재고_할당_상태의_전표에_취소를_요청하면_200_OK와_CANCELED_상태를_반환한다() throws Exception {
            // given
            Long outboundId = 1L;
            given(outboundService.cancel(outboundId))
                    .willReturn(outboundResponse(outboundId, OutboundStatus.CANCELED));

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/cancel", outboundId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outboundStatus").value("CANCELED"))
                    .andDo(document("outbound-cancel-success",
                            buildParams(OUTBOUND, "출고 취소", null, OUTBOUND_RESPONSE),
                            createResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 출고_준비_중_이상의_상태에서_취소를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(outboundService.cancel(anyLong()))
                    .willThrow(new OutboundCancelNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/cancel", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_CANCEL_NOT_ALLOWED"))
                    .andDo(document("outbound-cancel-fail-not-allowed",
                            buildErrorParams(OUTBOUND, "출고 취소"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_출고_ID로_취소를_요청하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            given(outboundService.cancel(anyLong()))
                    .willThrow(new OutboundNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/cancel", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_NOT_FOUND"))
                    .andDo(document("outbound-cancel-fail-not-found",
                            buildErrorParams(OUTBOUND, "출고 취소"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // ── Fixtures ──

    private static CreateOutboundRequestDto createRequest() {
        return new CreateOutboundRequestDto(
                1L, 10L, OutboundType.ORDER,
                List.of(new CreateOutboundRequestDto.OutboundItemRequestDto(1L, 1L, 5))
        );
    }

    private static OutboundResponseDto outboundResponse(Long id, OutboundStatus status) {
        return new OutboundResponseDto(id, 1L, 10L, OutboundType.ORDER, status, null);
    }

    // ── Field Descriptors ──

    private static FieldDescriptor[] getCreateOutboundRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("ordersId").description("발주 전표 ID (필수)"),
                fieldWithPath("warehouseId").description("출고 창고 ID (필수)"),
                fieldWithPath("outboundType").description("출고 유형 (ORDER: 가맹점 발주 출고)"),
                fieldWithPath("items").description("출고 품목 목록"),
                fieldWithPath("items[].orderItemId").description("발주 품목 ID"),
                fieldWithPath("items[].inventoryId").description("할당 재고 ID"),
                fieldWithPath("items[].targetQuantity").description("출고 지시 수량")
        };
    }

    private static FieldDescriptor[] getOutboundResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("outboundId").description("출고 전표 ID"),
                fieldWithPath("ordersId").description("발주 전표 ID"),
                fieldWithPath("warehouseId").description("출고 창고 ID"),
                fieldWithPath("outboundType").description("출고 유형 (ORDER)"),
                fieldWithPath("outboundStatus").description("출고 상태 (PENDING / ALLOCATED / PROCESSING / SHIPPED / CANCELED)"),
                fieldWithPath("outboundDate").description("출하 완료 일시 (출하 전 null)").optional()
        };
    }
}