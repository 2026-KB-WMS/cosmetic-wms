package com.kb.cosmetic_wms.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kb.cosmetic_wms.global.config.SecurityConfig;
import com.kb.cosmetic_wms.global.error.GlobalExceptionHandler;
import com.kb.cosmetic_wms.global.restdocs.RestDocsSupport;
import com.kb.cosmetic_wms.order.adapter.in.web.CreateOrderRequest;
import com.kb.cosmetic_wms.order.adapter.in.web.OrderController;
import com.kb.cosmetic_wms.order.application.port.in.OrderLifecycleUseCase;
import com.kb.cosmetic_wms.order.application.port.in.OrderResult;
import com.kb.cosmetic_wms.order.domain.enums.OrderStatus;
import com.kb.cosmetic_wms.order.domain.exception.*;
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

import java.util.List;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.kb.cosmetic_wms.global.restdocs.ApiDocs.ORDER;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.ORDER_CREATE_REQUEST;
import static com.kb.cosmetic_wms.global.restdocs.ApiSchemas.ORDER_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class OrderControllerTest extends RestDocsSupport {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderLifecycleUseCase orderLifecycleUseCase;

    @Nested
    class 발주_신청 {

        @Test
        @WithMockUser
        void 올바른_발주_정보가_주어지면_201_CREATED와_신청된_전표를_반환한다() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1L,
                    List.of(new CreateOrderRequest.OrderItemRequest(1L, 10))
            );
            OrderResult response = pendingOrderResponse(1L);
            given(orderLifecycleUseCase.createOrder(any())).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.orderStatus").value("PENDING"))
                    .andDo(document("order-create-success",
                            buildParams(ORDER, "발주 신청", ORDER_CREATE_REQUEST, ORDER_RESPONSE),
                            createRequestFields(getCreateOrderRequestFields()),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 가맹점_ID가_없으면_400_BAD_REQUEST를_반환한다() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    null,
                    List.of(new CreateOrderRequest.OrderItemRequest(1L, 10))
            );

            // when & then
            mockMvc.perform(post("/api/v1/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andDo(document("order-create-fail-no-store",
                            buildErrorParams(ORDER, "발주 신청"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 발주_품목이_비어있으면_400_BAD_REQUEST와_ORDER_ITEMS_REQUIRED를_반환한다() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(1L, List.of());
            given(orderLifecycleUseCase.createOrder(any())).willThrow(new OrderItemsRequiredException());

            // when & then
            mockMvc.perform(post("/api/v1/orders")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_ITEMS_REQUIRED"))
                    .andDo(document("order-create-fail-empty-items",
                            buildErrorParams(ORDER, "발주 신청"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 발주_확정 {

        @Test
        @WithMockUser
        void 발주_신청_상태의_전표를_확정하면_200_OK와_CONFIRMED_상태의_전표를_반환한다() throws Exception {
            // given
            Long orderId = 1L;
            OrderResult response = orderResponse(orderId, OrderStatus.CONFIRMED);
            given(orderLifecycleUseCase.confirmOrder(orderId)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", orderId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"))
                    .andDo(document("order-confirm-success",
                            buildParams(ORDER, "발주 확정", null, ORDER_RESPONSE),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 이미_확정된_전표에_재확정_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.confirmOrder(anyLong())).willThrow(new OrderConfirmNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_CONFIRM_NOT_ALLOWED"))
                    .andDo(document("order-confirm-fail-not-allowed",
                            buildErrorParams(ORDER, "발주 확정"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_발주_ID로_확정_요청하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.confirmOrder(anyLong())).willThrow(new OrderNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/confirm", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"))
                    .andDo(document("order-confirm-fail-not-found",
                            buildErrorParams(ORDER, "발주 확정"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 배송_준비_시작 {

        @Test
        @WithMockUser
        void 발주_확정_상태의_전표에_배송_준비_시작을_요청하면_200_OK와_PREPARING_상태를_반환한다() throws Exception {
            // given
            Long orderId = 1L;
            OrderResult response = orderResponse(orderId, OrderStatus.PREPARING);
            given(orderLifecycleUseCase.startPreparation(orderId)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/prepare", orderId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus").value("PREPARING"))
                    .andDo(document("order-prepare-success",
                            buildParams(ORDER, "배송 준비 시작", null, ORDER_RESPONSE),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 발주_확정_이외의_상태에서_배송_준비_시작을_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.startPreparation(anyLong())).willThrow(new OrderPreparationNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/prepare", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_PREPARATION_NOT_ALLOWED"))
                    .andDo(document("order-prepare-fail-not-allowed",
                            buildErrorParams(ORDER, "배송 준비 시작"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 배송_출하 {

        @Test
        @WithMockUser
        void 배송_준비_중_상태의_전표에_출하를_요청하면_200_OK와_SHIPPED_상태를_반환한다() throws Exception {
            // given
            Long orderId = 1L;
            OrderResult response = orderResponse(orderId, OrderStatus.SHIPPED);
            given(orderLifecycleUseCase.ship(orderId)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/ship", orderId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus").value("SHIPPED"))
                    .andDo(document("order-ship-success",
                            buildParams(ORDER, "배송 출하", null, ORDER_RESPONSE),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 배송_준비_중_이외의_상태에서_출하를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.ship(anyLong())).willThrow(new OrderShipNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/ship", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_SHIP_NOT_ALLOWED"))
                    .andDo(document("order-ship-fail-not-allowed",
                            buildErrorParams(ORDER, "배송 출하"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 배송_완료 {

        @Test
        @WithMockUser
        void 배송_중_상태의_전표에_배송_완료를_요청하면_200_OK와_DELIVERED_상태를_반환한다() throws Exception {
            // given
            Long orderId = 1L;
            OrderResult response = orderResponse(orderId, OrderStatus.DELIVERED);
            given(orderLifecycleUseCase.completeDelivery(orderId)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", orderId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus").value("DELIVERED"))
                    .andDo(document("order-deliver-success",
                            buildParams(ORDER, "배송 완료", null, ORDER_RESPONSE),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 배송_중_이외의_상태에서_배송_완료를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.completeDelivery(anyLong())).willThrow(new OrderDeliveryCompleteNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/deliver", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_DELIVERY_COMPLETE_NOT_ALLOWED"))
                    .andDo(document("order-deliver-fail-not-allowed",
                            buildErrorParams(ORDER, "배송 완료"),
                            globalErrorResponseFields()
                    ));
        }
    }

    @Nested
    class 발주_취소 {

        @Test
        @WithMockUser
        void 발주_신청_상태의_전표에_취소를_요청하면_200_OK와_CANCELED_상태를_반환한다() throws Exception {
            // given
            Long orderId = 1L;
            OrderResult response = orderResponse(orderId, OrderStatus.CANCELED);
            given(orderLifecycleUseCase.cancelOrder(orderId)).willReturn(response);

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus").value("CANCELED"))
                    .andDo(document("order-cancel-success",
                            buildParams(ORDER, "발주 취소", null, ORDER_RESPONSE),
                            createResponseFields(getOrderResponseFields())
                    ));
        }

        @Test
        @WithMockUser
        void 발주_확정_이후_상태에서_취소를_요청하면_409_CONFLICT를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.cancelOrder(anyLong())).willThrow(new OrderCancelNotAllowedException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", 1L)
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_CANCEL_NOT_ALLOWED"))
                    .andDo(document("order-cancel-fail-not-allowed",
                            buildErrorParams(ORDER, "발주 취소"),
                            globalErrorResponseFields()
                    ));
        }

        @Test
        @WithMockUser
        void 존재하지_않는_발주_ID로_취소_요청하면_404_NOT_FOUND를_반환한다() throws Exception {
            // given
            given(orderLifecycleUseCase.cancelOrder(anyLong())).willThrow(new OrderNotFoundException());

            // when & then
            mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", 999L)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"))
                    .andDo(document("order-cancel-fail-not-found",
                            buildErrorParams(ORDER, "발주 취소"),
                            globalErrorResponseFields()
                    ));
        }
    }

    // ── Fixtures ──

    private static OrderResult pendingOrderResponse(Long id) {
        return new OrderResult(
                id, 1L, null, OrderStatus.PENDING,
                List.of(new OrderResult.OrderItemResult(1L, 1L, 10))
        );
    }

    private static OrderResult orderResponse(Long id, OrderStatus status) {
        return new OrderResult(
                id, 1L, 10L, status,
                List.of(new OrderResult.OrderItemResult(1L, 1L, 10))
        );
    }

    // ── Field Descriptors ──

    private static FieldDescriptor[] getCreateOrderRequestFields() {
        return new FieldDescriptor[]{
                fieldWithPath("storeId").description("발주 요청 가맹점 ID (필수)"),
                fieldWithPath("items").description("발주 품목 목록 (최소 1개)"),
                fieldWithPath("items[].productId").description("발주 품목의 상품 ID"),
                fieldWithPath("items[].quantity").description("발주 수량")
        };
    }

    private static FieldDescriptor[] getOrderResponseFields() {
        return new FieldDescriptor[]{
                fieldWithPath("id").description("발주 전표 ID"),
                fieldWithPath("storeId").description("가맹점 ID"),
                fieldWithPath("warehouseId").type(JsonFieldType.NUMBER).optional()
                        .description("배정 물류창고 ID (자동 배정 전에는 null)"),
                fieldWithPath("orderStatus").description("발주 상태 (PENDING / CONFIRMED / PREPARING / SHIPPED / DELIVERED / CANCELED)"),
                fieldWithPath("items").description("발주 품목 목록"),
                fieldWithPath("items[].id").description("발주 품목 ID"),
                fieldWithPath("items[].productId").description("상품 ID"),
                fieldWithPath("items[].quantity").description("발주 수량")
        };
    }
}