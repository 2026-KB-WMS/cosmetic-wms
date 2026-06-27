package com.kb.cosmetic_wms.inbound;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.global.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inbound.application.exception.InboundPartnerNotFoundException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundWarehouseNotFoundException;
import com.kb.cosmetic_wms.inbound.application.port.in.*;
import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.application.port.out.PartnerQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.inbound.application.service.InboundService;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import com.kb.cosmetic_wms.inbound.fixture.InboundTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @InjectMocks
    private InboundService inboundService;

    @Mock
    private InboundPort inboundPort;
    @Mock
    private StorageQueryPort storagePort;
    @Mock
    private PartnerQueryPort partnerPort;
    @Mock
    private ProductQueryPort productQueryPort;
    @Mock
    private EventPublisher eventPublisher;

    private Inbound defaultInbound;

    @BeforeEach
    void setUp() {
        defaultInbound = new InboundTestBuilder().id(1L).build();
    }

    // =========================================================
    // 입고 전표 등록
    // =========================================================

    @Nested
    class 입고_전표_등록 {

        @Test
        void 창고와_파트너가_존재하면_SCHEDULED_상태로_입고_전표가_등록된다() {
            // given
            RegisterInboundCommand command = new RegisterInboundCommand(1L, 1L, LocalDateTime.now().plusDays(1));

            given(storagePort.existsById(1L)).willReturn(true);
            given(partnerPort.existsById(1L)).willReturn(true);
            given(inboundPort.save(any(Inbound.class))).willReturn(defaultInbound);

            // when
            InboundResult result = inboundService.register(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
            assertThat(result.warehouseId()).isEqualTo(1L);
            assertThat(result.partnerId()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_창고_ID로_등록하면_WarehouseNotFoundException이_발생한다() {
            // given
            RegisterInboundCommand command = new RegisterInboundCommand(999L, 1L, LocalDateTime.now().plusDays(1));
            given(storagePort.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> inboundService.register(command))
                    .isInstanceOf(InboundWarehouseNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_WAREHOUSE_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_파트너_ID로_등록하면_PartnerNotFoundException이_발생한다() {
            // given
            RegisterInboundCommand command = new RegisterInboundCommand(1L, 999L, LocalDateTime.now().plusDays(1));
            given(storagePort.existsById(1L)).willReturn(true);
            given(partnerPort.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> inboundService.register(command))
                    .isInstanceOf(InboundPartnerNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_PARTNER_NOT_FOUND.getMessage());
        }
    }

    // =========================================================
    // 입고 품목 추가
    // =========================================================

    @Nested
    class 입고_품목_추가 {

        @Test
        void SCHEDULED_상태의_입고_전표에_품목을_추가하면_WAITING_상태의_품목이_포함된_전표가_반환된다() {
            // given
            AddInboundItemCommand command = new AddInboundItemCommand(1L, 100,
                    LocalDate.now().minusDays(10), LocalDate.now().plusYears(2));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));
            given(productQueryPort.existsById(1L)).willReturn(true);
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundResult result = inboundService.addItem(1L, command);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productId()).isEqualTo(1L);
            assertThat(result.items().get(0).inspectionStatus()).isEqualTo(InspectionStatus.WAITING);
        }

        @Test
        void 존재하지_않는_입고_ID로_품목을_추가하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundPort.findByIdWithItems(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(999L,
                    new AddInboundItemCommand(1L, 100, LocalDate.now().minusDays(10), LocalDate.now().plusYears(2))))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 존재하지_않는_상품_ID로_품목을_추가하면_InboundProductNotFoundException이_발생한다() {
            // given
            AddInboundItemCommand command = new AddInboundItemCommand(999L, 100,
                    LocalDate.now().minusDays(10), LocalDate.now().plusYears(2));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));
            given(productQueryPort.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(1L, command))
                    .isInstanceOf(InboundProductNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_PRODUCT_NOT_FOUND.getMessage());
        }

        @Test
        void SCHEDULED_이외_상태의_입고_전표에_품목_추가를_시도하면_InboundInvalidAddItemStatusException이_발생한다() {
            // given - IN_PROGRESS 상태의 입고 전표 (품목 포함)
            Inbound inProgress = new InboundTestBuilder().id(1L).buildInProgress();
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inProgress));
            given(productQueryPort.existsById(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> inboundService.addItem(1L,
                    new AddInboundItemCommand(1L, 100, LocalDate.now().minusDays(10), LocalDate.now().plusYears(2))))
                    .isInstanceOf(InboundInvalidAddItemStatusException.class)
                    .hasMessage(InboundErrorCode.INBOUND_INVALID_ADD_ITEM_STATUS.getMessage());
        }
    }

    // =========================================================
    // 입고 작업 시작
    // =========================================================

    @Nested
    class 입고_작업_시작 {

        @Test
        void SCHEDULED_상태이고_품목이_존재하면_IN_PROGRESS_상태로_전환된다() {
            // given - 품목이 있는 SCHEDULED 입고 전표
            Inbound inbound = new InboundTestBuilder().id(1L).build();
            inbound.addItem(new InboundLine(1L, 100, LocalDate.now().minusDays(1), LocalDate.now().plusYears(1)));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundResult result = inboundService.start(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.IN_PROGRESS);
        }

        @Test
        void 품목이_없는_입고_전표를_시작하려고_하면_InboundEmptyItemsException이_발생한다() {
            // given - 품목이 없는 SCHEDULED 입고 전표
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.start(1L))
                    .isInstanceOf(InboundEmptyItemsException.class)
                    .hasMessage(InboundErrorCode.INBOUND_EMPTY_ITEMS.getMessage());
        }

        @Test
        void 존재하지_않는_입고_ID로_작업_시작을_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundPort.findByIdWithItems(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.start(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_진행_중인_입고_전표에_작업_시작을_요청하면_InboundInvalidStartStatusException이_발생한다() {
            // given - 품목을 가진 IN_PROGRESS 입고 전표
            Inbound inProgress = new InboundTestBuilder().id(1L).buildInProgress();
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inProgress));

            // when & then
            assertThatThrownBy(() -> inboundService.start(1L))
                    .isInstanceOf(InboundInvalidStartStatusException.class)
                    .hasMessageContaining("입고 예정 상태에서만 작업을 시작할 수 있습니다");
        }
    }

    // =========================================================
    // 실물 적재 완료 (Putaway)
    // =========================================================

    @Nested
    class 실물_적재_완료 {

        @Test
        void WAITING_상태의_품목에_로트와_섹션을_지정하면_INSPECTING_상태로_전환되고_ID가_할당된다() {
            // given - ID가 있는 WAITING 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.WAITING, null, null);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.SCHEDULED,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundItemResult result = inboundService.completePutaway(1L, 1L, new PutawayCommand(100L, 200L));

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.INSPECTING);
            assertThat(result.lotId()).isEqualTo(100L);
            assertThat(result.sectionId()).isEqualTo(200L);
        }

        @Test
        void 존재하지_않는_품목_ID로_적재_완료를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completePutaway(1L, 999L, new PutawayCommand(100L, 200L)))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_적재가_완료된_품목에_다시_적재를_요청하면_InboundInvalidPutawayStatusException이_발생한다() {
            // given - INSPECTING 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.INSPECTING, 100L, 200L);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.completePutaway(1L, 1L, new PutawayCommand(100L, 200L)))
                    .isInstanceOf(InboundInvalidPutawayStatusException.class)
                    .hasMessageContaining("이미 적재가 완료되었거나 검수가 진행된 품목입니다");
        }
    }

    // =========================================================
    // 검수 정상 완료
    // =========================================================

    @Nested
    class 검수_정상_완료 {

        @Test
        void INSPECTING_상태의_품목을_정상_완료하면_NORMAL_상태로_전환된다() {
            // given - INSPECTING 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.INSPECTING, 100L, 200L);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundItemResult result = inboundService.approve(1L, 1L);

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.NORMAL);
        }

        @Test
        void 존재하지_않는_품목_ID로_검수_정상_처리를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.approve(1L, 999L))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 적재_전_WAITING_상태의_품목에_정상_완료를_요청하면_InboundInvalidApproveStatusException이_발생한다() {
            // given - WAITING 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.WAITING, null, null);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.SCHEDULED,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.approve(1L, 1L))
                    .isInstanceOf(InboundInvalidApproveStatusException.class)
                    .hasMessage(InboundErrorCode.INBOUND_INVALID_APPROVE_STATUS.getMessage());
        }
    }

    // =========================================================
    // 검수 보류
    // =========================================================

    @Nested
    class 검수_보류 {

        @Test
        void INSPECTING_상태의_품목을_보류_처리하면_HOLD_상태로_전환된다() {
            // given - INSPECTING 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.INSPECTING, 100L, 200L);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundItemResult result = inboundService.hold(1L, 1L);

            // then
            assertThat(result.inspectionStatus()).isEqualTo(InspectionStatus.HOLD);
        }

        @Test
        void 존재하지_않는_품목_ID로_검수_보류를_요청하면_InboundItemNotFoundException이_발생한다() {
            // given - 품목이 없는 Inbound
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.hold(1L, 999L))
                    .isInstanceOf(InboundItemNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_ITEM_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_NORMAL_상태인_품목에_보류를_요청하면_InboundInvalidHoldStatusException이_발생한다() {
            // given - NORMAL 상태 품목을 가진 Inbound
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(2),
                    InspectionStatus.NORMAL, 100L, 200L);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.hold(1L, 1L))
                    .isInstanceOf(InboundInvalidHoldStatusException.class)
                    .hasMessage(InboundErrorCode.INBOUND_INVALID_HOLD_STATUS.getMessage());
        }
    }

    // =========================================================
    // 입고 완료
    // =========================================================

    @Nested
    class 입고_완료 {

        @Test
        void 모든_품목이_NORMAL_상태이면_입고가_COMPLETED_상태로_전환된다() {
            // given - 모든 품목이 NORMAL인 IN_PROGRESS 입고
            Inbound inbound = buildInboundWithInspectedItem(true);
            given(inboundPort.findByIdWithItemsForUpdate(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundResult result = inboundService.complete(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.COMPLETED);
        }

        @Test
        void 품목이_HOLD_상태여도_검수가_완료되었으면_입고_완료_처리된다() {
            // given - 품목이 HOLD인 IN_PROGRESS 입고
            Inbound inbound = buildInboundWithHoldItem();
            given(inboundPort.findByIdWithItemsForUpdate(1L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundResult result = inboundService.complete(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.COMPLETED);
        }

        @Test
        void 검수_미완료_품목이_남아있으면_입고_완료_처리_시_InboundInspectionIncompleteException이_발생한다() {
            // given - WAITING 상태 품목이 포함된 IN_PROGRESS 입고
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(1),
                    InspectionStatus.WAITING, null, null);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItemsForUpdate(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.complete(1L))
                    .isInstanceOf(InboundInspectionIncompleteException.class)
                    .hasMessage(InboundErrorCode.INBOUND_INSPECTION_INCOMPLETE.getMessage());
        }

        @Test
        void 존재하지_않는_입고_ID로_완료를_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundPort.findByIdWithItemsForUpdate(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.complete(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void SCHEDULED_상태의_입고에_완료를_요청하면_InboundInvalidCompleteStatusException이_발생한다() {
            // given - SCHEDULED(기본) 상태 그대로 사용
            given(inboundPort.findByIdWithItemsForUpdate(1L)).willReturn(Optional.of(defaultInbound));

            // when & then
            assertThatThrownBy(() -> inboundService.complete(1L))
                    .isInstanceOf(InboundInvalidCompleteStatusException.class)
                    .hasMessageContaining("작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다");
        }

        @Test
        void 입고_완료_시_InboundCompletedEvent가_발행된다() {
            // given
            Inbound inbound = buildInboundWithInspectedItem(true, 10L);
            given(inboundPort.findByIdWithItemsForUpdate(10L)).willReturn(Optional.of(inbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            inboundService.complete(10L);

            // then
            ArgumentCaptor<InboundCompletedEvent> captor = ArgumentCaptor.forClass(InboundCompletedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            InboundCompletedEvent event = captor.getValue();
            assertThat(event.inboundId()).isEqualTo(10L);
            assertThat(event.warehouseId()).isEqualTo(1L);
            assertThat(event.items()).hasSize(1);
            assertThat(event.items().get(0).quantity()).isEqualTo(100);
        }

        @Test
        void 입고_완료_실패_시_이벤트가_발행되지_않는다() {
            // given - 검수 미완료 품목이 존재하는 IN_PROGRESS 입고
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(1),
                    InspectionStatus.WAITING, null, null);
            Inbound inbound = Inbound.reconstitute(1L, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
            given(inboundPort.findByIdWithItemsForUpdate(1L)).willReturn(Optional.of(inbound));

            // when & then
            assertThatThrownBy(() -> inboundService.complete(1L))
                    .isInstanceOf(InboundInspectionIncompleteException.class);

            verify(eventPublisher, never()).publish(any());
        }

        private Inbound buildInboundWithInspectedItem(boolean isNormal) {
            return buildInboundWithInspectedItem(isNormal, 1L);
        }

        private Inbound buildInboundWithInspectedItem(boolean isNormal, Long inboundId) {
            InspectionStatus status = isNormal ? InspectionStatus.NORMAL : InspectionStatus.HOLD;
            InboundItem item = InboundItem.reconstitute(1L, 1L, 100,
                    LocalDate.now().minusDays(1), LocalDate.now().plusYears(1),
                    status, 100L, 200L);
            return Inbound.reconstitute(inboundId, InboundStatus.IN_PROGRESS,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(item));
        }

        private Inbound buildInboundWithHoldItem() {
            return buildInboundWithInspectedItem(false);
        }
    }

    // =========================================================
    // 입고 취소
    // =========================================================

    @Nested
    class 입고_취소 {

        @Test
        void SCHEDULED_상태의_입고_전표를_취소하면_CANCELED_상태로_전환된다() {
            // given
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            InboundResult result = inboundService.cancel(1L);

            // then
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.CANCELED);
        }

        @Test
        void 존재하지_않는_입고_ID로_취소를_요청하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundPort.findByIdWithItems(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.cancel(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_진행_중인_입고에_취소를_요청하면_InboundInvalidCancelStatusException이_발생한다() {
            // given
            Inbound inProgress = new InboundTestBuilder().id(1L).buildInProgress();
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(inProgress));

            // when & then
            assertThatThrownBy(() -> inboundService.cancel(1L))
                    .isInstanceOf(InboundInvalidCancelStatusException.class)
                    .hasMessageContaining("이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다");
        }
    }

    // =========================================================
    // 입고 단건 조회
    // =========================================================

    @Nested
    class 입고_단건_조회 {

        @Test
        void 존재하는_ID로_조회하면_입고_전표_상세정보를_반환한다() {
            // given
            given(inboundPort.findByIdWithItems(1L)).willReturn(Optional.of(defaultInbound));

            // when
            InboundResult result = inboundService.findById(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
            assertThat(result.warehouseId()).isEqualTo(1L);
            assertThat(result.partnerId()).isEqualTo(1L);
            assertThat(result.items()).isEmpty();
        }

        @Test
        void 존재하지_않는_ID로_조회하면_InboundNotFoundException이_발생한다() {
            // given
            given(inboundPort.findByIdWithItems(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inboundService.findById(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }
    }
}
