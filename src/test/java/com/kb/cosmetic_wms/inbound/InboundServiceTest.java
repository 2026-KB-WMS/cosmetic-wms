package com.kb.cosmetic_wms.inbound;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.inbound.domain.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.inbound.application.exception.InboundCapacityExceededException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundPartnerNotFoundException;
import com.kb.cosmetic_wms.inbound.application.exception.InboundWarehouseNotFoundException;
import com.kb.cosmetic_wms.inbound.application.port.in.InboundResult;
import com.kb.cosmetic_wms.inbound.application.port.in.ReceiveInboundCommand;
import com.kb.cosmetic_wms.inbound.application.port.in.RegisterInboundCommand;
import com.kb.cosmetic_wms.inbound.application.port.out.InboundPort;
import com.kb.cosmetic_wms.inbound.application.port.out.PartnerQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.inbound.application.port.out.StorageQueryPort;
import com.kb.cosmetic_wms.inbound.application.service.InboundService;
import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

        private RegisterInboundCommand validCommand() {
            return new RegisterInboundCommand(1L, 1L, LocalDateTime.now().plusDays(1),
                    List.of(new RegisterInboundCommand.LineItem(1L, 100)));
        }

        @Test
        void 창고와_파트너와_상품이_존재하면_SCHEDULED_상태로_입고_전표가_등록된다() {
            given(storagePort.existsById(1L)).willReturn(true);
            given(partnerPort.existsById(1L)).willReturn(true);
            given(productQueryPort.allExistByIds(anyCollection())).willReturn(true);
            given(inboundPort.save(any(Inbound.class))).willReturn(defaultInbound);

            InboundResult result = inboundService.register(validCommand());

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
        }

        @Test
        void 존재하지_않는_창고_ID로_등록하면_InboundWarehouseNotFoundException이_발생한다() {
            RegisterInboundCommand command = new RegisterInboundCommand(999L, 1L,
                    LocalDateTime.now().plusDays(1),
                    List.of(new RegisterInboundCommand.LineItem(1L, 100)));
            given(storagePort.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> inboundService.register(command))
                    .isInstanceOf(InboundWarehouseNotFoundException.class);
        }

        @Test
        void 존재하지_않는_파트너_ID로_등록하면_InboundPartnerNotFoundException이_발생한다() {
            RegisterInboundCommand command = new RegisterInboundCommand(1L, 999L,
                    LocalDateTime.now().plusDays(1),
                    List.of(new RegisterInboundCommand.LineItem(1L, 100)));
            given(storagePort.existsById(1L)).willReturn(true);
            given(partnerPort.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> inboundService.register(command))
                    .isInstanceOf(InboundPartnerNotFoundException.class);
        }

        @Test
        void 존재하지_않는_상품_ID로_등록하면_InboundProductNotFoundException이_발생한다() {
            RegisterInboundCommand command = new RegisterInboundCommand(1L, 1L,
                    LocalDateTime.now().plusDays(1),
                    List.of(new RegisterInboundCommand.LineItem(999L, 100)));
            given(storagePort.existsById(1L)).willReturn(true);
            given(partnerPort.existsById(1L)).willReturn(true);
            given(productQueryPort.allExistByIds(anyCollection())).willReturn(false);

            assertThatThrownBy(() -> inboundService.register(command))
                    .isInstanceOf(InboundProductNotFoundException.class);
        }
    }

    // =========================================================
    // 수령 확인
    // =========================================================

    @Nested
    class 수령_확인 {

        private Inbound scheduledInboundWithLine(Long lineId) {
            InboundLine line = InboundLine.reconstitute(lineId, 1L, 100, 0, null, null, null);
            return Inbound.reconstitute(1L, InboundStatus.SCHEDULED,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(line));
        }

        private ReceiveInboundCommand receiveCommand(Long lineId, int qty) {
            return new ReceiveInboundCommand(List.of(new ReceiveInboundCommand.LineItem(
                    lineId, qty, "LOT0001",
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2))));
        }

        @Test
        void SCHEDULED_상태의_입고_전표에_수령_확인을_하면_RECEIVED_상태로_전환되고_이벤트가_발행된다() {
            Inbound inbound = scheduledInboundWithLine(1L);
            given(inboundPort.findByIdWithLinesForUpdate(1L)).willReturn(Optional.of(inbound));
            given(storagePort.canAccommodate(anyLong(), anyList())).willReturn(true);
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            ReceiveInboundCommand command = receiveCommand(1L, 95);

            InboundResult result = inboundService.receive(1L, command);

            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.RECEIVED);
            assertThat(result.lines().get(0).receivedQuantity()).isEqualTo(95);
            verify(eventPublisher).publish(any(InboundCompletedEvent.class));
        }

        @Test
        void 수령_확인_시_InboundCompletedEvent에_partnerId와_LineSnapshot이_포함된다() {
            Inbound inbound = scheduledInboundWithLine(10L);
            given(inboundPort.findByIdWithLinesForUpdate(1L)).willReturn(Optional.of(inbound));
            given(storagePort.canAccommodate(anyLong(), anyList())).willReturn(true);
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            inboundService.receive(1L, receiveCommand(10L, 80));

            ArgumentCaptor<InboundCompletedEvent> captor = ArgumentCaptor.forClass(InboundCompletedEvent.class);
            verify(eventPublisher).publish(captor.capture());
            InboundCompletedEvent event = captor.getValue();

            assertThat(event.partnerId()).isEqualTo(1L);
            assertThat(event.lines()).hasSize(1);
            assertThat(event.lines().get(0).receivedQuantity()).isEqualTo(80);
            assertThat(event.lines().get(0).orderedQuantity()).isEqualTo(100);
        }

        @Test
        void 존재하지_않는_입고_ID로_수령_확인을_요청하면_InboundNotFoundException이_발생한다() {
            given(inboundPort.findByIdWithLinesForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inboundService.receive(999L, new ReceiveInboundCommand(List.of())))
                    .isInstanceOf(InboundNotFoundException.class);
        }

        @Test
        void RECEIVED_상태에서_수령_확인을_다시_시도하면_InboundInvalidReceiveStatusException이_발생한다() {
            InboundLine line = InboundLine.reconstitute(1L, 1L, 100, 100, "LOT0001",
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(2));
            Inbound alreadyReceived = Inbound.reconstitute(1L, InboundStatus.RECEIVED,
                    LocalDateTime.now().plusDays(1), 1L, 1L, List.of(line));
            given(inboundPort.findByIdWithLinesForUpdate(1L)).willReturn(Optional.of(alreadyReceived));
            given(storagePort.canAccommodate(anyLong(), anyList())).willReturn(true);

            assertThatThrownBy(() -> inboundService.receive(1L, receiveCommand(1L, 50)))
                    .isInstanceOf(InboundInvalidReceiveStatusException.class);
        }

        @Test
        void 요청의_라인_ID가_입고_전표와_불일치하면_InboundReceiveLineMismatchException이_발생한다() {
            Inbound inbound = scheduledInboundWithLine(1L);
            given(inboundPort.findByIdWithLinesForUpdate(1L)).willReturn(Optional.of(inbound));
            given(storagePort.canAccommodate(anyLong(), anyList())).willReturn(true);

            ReceiveInboundCommand wrongCommand = receiveCommand(999L, 50);

            assertThatThrownBy(() -> inboundService.receive(1L, wrongCommand))
                    .isInstanceOf(InboundReceiveLineMismatchException.class);

            verify(eventPublisher, never()).publish(any());
        }

        @Test
        void 창고의_DOCKING_수용량이_부족하면_InboundCapacityExceededException이_발생한다() {
            Inbound inbound = scheduledInboundWithLine(1L);
            given(inboundPort.findByIdWithLinesForUpdate(1L)).willReturn(Optional.of(inbound));
            given(storagePort.canAccommodate(anyLong(), anyList())).willReturn(false);

            assertThatThrownBy(() -> inboundService.receive(1L, receiveCommand(1L, 95)))
                    .isInstanceOf(InboundCapacityExceededException.class);

            verify(eventPublisher, never()).publish(any());
        }
    }

    // =========================================================
    // 입고 취소
    // =========================================================

    @Nested
    class 입고_취소 {

        @Test
        void SCHEDULED_상태의_입고_전표를_취소하면_CANCELED_상태로_전환된다() {
            given(inboundPort.findByIdWithLines(1L)).willReturn(Optional.of(defaultInbound));
            given(inboundPort.save(any(Inbound.class))).willAnswer(inv -> inv.getArgument(0));

            InboundResult result = inboundService.cancel(1L);

            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.CANCELED);
        }

        @Test
        void 존재하지_않는_입고_ID로_취소를_요청하면_InboundNotFoundException이_발생한다() {
            given(inboundPort.findByIdWithLines(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inboundService.cancel(999L))
                    .isInstanceOf(InboundNotFoundException.class);
        }

        @Test
        void RECEIVED_상태의_입고에_취소를_요청하면_InboundInvalidCancelStatusException이_발생한다() {
            Inbound received = new InboundTestBuilder().id(1L).status(InboundStatus.RECEIVED).build();
            given(inboundPort.findByIdWithLines(1L)).willReturn(Optional.of(received));

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
            given(inboundPort.findByIdWithLines(1L)).willReturn(Optional.of(defaultInbound));

            InboundResult result = inboundService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.inboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_InboundNotFoundException이_발생한다() {
            given(inboundPort.findByIdWithLines(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> inboundService.findById(999L))
                    .isInstanceOf(InboundNotFoundException.class)
                    .hasMessage(InboundErrorCode.INBOUND_NOT_FOUND.getMessage());
        }
    }
}
