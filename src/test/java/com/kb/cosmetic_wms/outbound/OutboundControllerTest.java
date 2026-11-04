package com.kb.cosmetic_wms.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.support.TestSecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.outbound.adapter.in.web.CreateOutboundRequest;
import com.kb.cosmetic_wms.outbound.adapter.in.web.OutboundController;
import com.kb.cosmetic_wms.outbound.application.port.in.FindOutboundUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundLifecycleUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundDetailResult;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.in.RecordPickingUseCase;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.exception.*;
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
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.OUTBOUND_DETAIL_RESPONSE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.OUTBOUND_RESPONSE;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.RECORD_PICKING_REQUEST;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OutboundController.class})
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
public class OutboundControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OutboundLifecycleUseCase outboundLifecycleUseCase;

    @MockitoBean
    private FindOutboundUseCase findOutboundUseCase;

    @MockitoBean
    private RecordPickingUseCase recordPickingUseCase;

    @Nested
    class 출고_상세_조회 {

        @Test
        @WithMockUser
        void 품목을_포함한_출고_상세를_반환하고_API_문서가_생성된다() throws Exception {
            given(findOutboundUseCase.findById(1L)).willReturn(detailResult(OutboundStatus.PROCESSING, 4));

            mockMvc.perform(get("/api/v1/outbounds/{outboundId}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.outboundStatus").value("PROCESSING"))
                    .andExpect(jsonPath("$.items[0].outboundItemId").value(11L))
                    .andExpect(jsonPath("$.items[0].inventoryId").value(5L))
                    .andExpect(jsonPath("$.items[0].targetQuantity").value(10))
                    .andExpect(jsonPath("$.items[0].pickedQuantity").value(4))
                    .andDo(document("outbound-detail-success",
                            buildParams(OUTBOUND, "출고 상세 조회", null, OUTBOUND_DETAIL_RESPONSE),
                            createResponseFields(getOutboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_출고_ID로_조회하면_404를_반환한다() throws Exception {
            given(findOutboundUseCase.findById(anyLong())).willThrow(new OutboundNotFoundException());

            mockMvc.perform(get("/api/v1/outbounds/{outboundId}", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_NOT_FOUND"))
                    .andDo(document("outbound-detail-fail-not-found",
                            buildErrorParams(OUTBOUND, "출고 상세 조회"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 피킹_수량_기록 {

        @Test
        @WithMockUser
        void 피킹_수량을_설정하면_갱신된_출고_상세를_반환하고_API_문서가_생성된다() throws Exception {
            given(recordPickingUseCase.recordPicking(1L, 11L, 7)).willReturn(detailResult(OutboundStatus.PROCESSING, 7));

            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/items/{outboundItemId}/picking", 1L, 11L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"pickedQuantity\": 7}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].pickedQuantity").value(7))
                    .andDo(document("outbound-picking-success",
                            buildParams(OUTBOUND, "피킹 수량 기록", RECORD_PICKING_REQUEST, OUTBOUND_DETAIL_RESPONSE),
                            createRequestFields(fieldWithPath("pickedQuantity")
                                    .description("실제 피킹 수량. 누적이 아니라 절대값으로 설정된다 (0 이상, 출고 지시 수량 이하)")),
                            createResponseFields(getOutboundDetailResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 피킹_수량이_없으면_400을_반환한다() throws Exception {
            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/items/{outboundItemId}/picking", 1L, 11L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
        }

        @Test
        @WithMockUser
        void 출고_준비_중이_아니면_409를_반환한다() throws Exception {
            given(recordPickingUseCase.recordPicking(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                    .willThrow(new OutboundPickingNotAllowedException());

            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/items/{outboundItemId}/picking", 1L, 11L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"pickedQuantity\": 7}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_PICKING_NOT_ALLOWED"))
                    .andDo(document("outbound-picking-fail-not-allowed",
                            buildErrorParams(OUTBOUND, "피킹 수량 기록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 지시_수량을_초과하면_400을_반환한다() throws Exception {
            given(recordPickingUseCase.recordPicking(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                    .willThrow(new OutboundExceedPickedQuantityException());

            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/items/{outboundItemId}/picking", 1L, 11L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"pickedQuantity\": 99}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_EXCEED_PICKED_QUANTITY"))
                    .andDo(document("outbound-picking-fail-exceed",
                            buildErrorParams(OUTBOUND, "피킹 수량 기록"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_품목이면_404를_반환한다() throws Exception {
            given(recordPickingUseCase.recordPicking(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyInt()))
                    .willThrow(new OutboundItemNotFoundException(999L));

            mockMvc.perform(patch("/api/v1/outbounds/{outboundId}/items/{outboundItemId}/picking", 1L, 999L)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"pickedQuantity\": 1}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("OUTBOUND_ITEM_NOT_FOUND"))
                    .andDo(document("outbound-picking-fail-item-not-found",
                            buildErrorParams(OUTBOUND, "피킹 수량 기록"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 로트별_진행_중_출고_조회 {

        @Test
        @WithMockUser
        void 로트에_할당된_진행_중_출고_전표_목록을_반환하고_API_문서가_생성된다() throws Exception {
            // given
            given(findOutboundUseCase.findInProgressByLotId(10L)).willReturn(List.of(
                    outboundResult(101L, OutboundStatus.ALLOCATED),
                    outboundResult(102L, OutboundStatus.PROCESSING)));

            // when & then
            mockMvc.perform(get("/api/v1/outbounds").param("lotId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].outboundId").value(101L))
                    .andExpect(jsonPath("$[0].outboundStatus").value("ALLOCATED"))
                    .andExpect(jsonPath("$[1].outboundStatus").value("PROCESSING"))
                    .andDo(document("outbound-list-by-lot",
                            buildParams(OUTBOUND, "로트에 할당된 진행 중 출고 전표 조회 (리콜·보류 대응)", null, OUTBOUND_RESPONSE),
                            createListResponseFields(getOutboundResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 할당된_출고가_없으면_빈_목록을_반환한다() throws Exception {
            given(findOutboundUseCase.findInProgressByLotId(10L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/outbounds").param("lotId", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithMockUser
        void 로트_ID가_없으면_400을_반환한다() throws Exception {
            mockMvc.perform(get("/api/v1/outbounds"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        void 로트_ID가_숫자가_아니면_400을_반환한다() throws Exception {
            mockMvc.perform(get("/api/v1/outbounds").param("lotId", "abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 출고_의뢰_등록 {

        @Test
        @WithMockUser
        void 올바른_출고_정보가_주어지면_201_CREATED와_PENDING_상태의_전표를_반환한다() throws Exception {
            // given
            CreateOutboundRequest request = createRequest();
            given(outboundLifecycleUseCase.createOutbound(any()))
                    .willReturn(outboundResult(1L, OutboundStatus.PENDING));

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
            CreateOutboundRequest request = new CreateOutboundRequest(
                    null, 10L, OutboundType.ORDER,
                    List.of(new CreateOutboundRequest.OutboundItemRequest(1L, 1L, 5))
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
            CreateOutboundRequest request = new CreateOutboundRequest(
                    1L, null, OutboundType.ORDER,
                    List.of(new CreateOutboundRequest.OutboundItemRequest(1L, 1L, 5))
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
            CreateOutboundRequest request = new CreateOutboundRequest(
                    1L, 10L, null,
                    List.of(new CreateOutboundRequest.OutboundItemRequest(1L, 1L, 5))
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
            given(outboundLifecycleUseCase.allocateInventory(outboundId))
                    .willReturn(outboundResult(outboundId, OutboundStatus.ALLOCATED));

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
            given(outboundLifecycleUseCase.allocateInventory(anyLong()))
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
            given(outboundLifecycleUseCase.allocateInventory(anyLong()))
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
            given(outboundLifecycleUseCase.startProcessing(outboundId))
                    .willReturn(outboundResult(outboundId, OutboundStatus.PROCESSING));

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
            given(outboundLifecycleUseCase.startProcessing(anyLong()))
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
            given(outboundLifecycleUseCase.ship(outboundId))
                    .willReturn(outboundResult(outboundId, OutboundStatus.SHIPPED));

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
            given(outboundLifecycleUseCase.ship(anyLong()))
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
            given(outboundLifecycleUseCase.cancel(outboundId))
                    .willReturn(outboundResult(outboundId, OutboundStatus.CANCELED));

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
            given(outboundLifecycleUseCase.cancel(anyLong()))
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
            given(outboundLifecycleUseCase.cancel(anyLong()))
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

    private static CreateOutboundRequest createRequest() {
        return new CreateOutboundRequest(
                1L, 10L, OutboundType.ORDER,
                List.of(new CreateOutboundRequest.OutboundItemRequest(1L, 1L, 5))
        );
    }

    private static OutboundResult outboundResult(Long id, OutboundStatus status) {
        return new OutboundResult(id, 1L, 10L, OutboundType.ORDER, status, null);
    }

    private static OutboundDetailResult detailResult(OutboundStatus status, int picked) {
        return new OutboundDetailResult(1L, 1L, 10L, OutboundType.ORDER, status, null,
                List.of(new OutboundDetailResult.Item(11L, 3L, 5L, 10, picked)));
    }

    // ── Field Descriptors ──

    private static FieldDescriptor[] getOutboundDetailResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("outboundId").description("출고 전표 ID"),
                fieldWithPath("ordersId").description("발주 전표 ID"),
                fieldWithPath("warehouseId").description("출고 창고 ID"),
                fieldWithPath("outboundType").description("출고 유형 (ORDER)"),
                fieldWithPath("outboundStatus").description("출고 상태 (PENDING / ALLOCATED / PROCESSING / SHIPPED / CANCELED)"),
                fieldWithPath("outboundDate").description("출하 완료 일시 (출하 전 null)").optional(),
                fieldWithPath("items").description("출고 품목 목록"),
                fieldWithPath("items[].outboundItemId").description("출고 품목 ID (피킹 수량 기록 시 사용)"),
                fieldWithPath("items[].orderItemId").description("발주 품목 ID"),
                fieldWithPath("items[].inventoryId").description("할당 재고 ID"),
                fieldWithPath("items[].targetQuantity").description("출고 지시 수량"),
                fieldWithPath("items[].pickedQuantity").description("실제 피킹 수량")
        };
    }

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
