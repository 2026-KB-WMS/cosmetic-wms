package com.kb.cosmetic_wms.domain.outbound;

import com.kb.cosmetic_wms.domain.outbound.constants.OutboundConstants;
import com.kb.cosmetic_wms.domain.outbound.dto.OutboundResponseDto;
import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundStatus;
import com.kb.cosmetic_wms.domain.outbound.enums.OutboundType;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundAllocatedEvent;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundCanceledEvent;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundShippedEvent;
import com.kb.cosmetic_wms.domain.outbound.event.OutboundStockReleaseRequestedEvent;
import com.kb.cosmetic_wms.domain.outbound.exception.*;
import com.kb.cosmetic_wms.domain.outbound.fixture.OutboundTestBuilder;
import com.kb.cosmetic_wms.domain.outbound.repository.OutboundRepository;
import com.kb.cosmetic_wms.domain.outbound.service.OutboundService;
import com.kb.cosmetic_wms.global.event.EventPublisher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboundServiceTest {

    @InjectMocks
    private OutboundService outboundService;

    @Mock
    private OutboundRepository outboundRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Nested
    class 출고_의뢰_등록 {

        @Test
        void 가맹점_발주_확정_이벤트를_수신하면_ORDER_타입의_출고_대기_상태_전표가_성공적으로_생성된다() {
            // given
            List<OutboundLine> lines = List.of(new OutboundLine(1L, 1L, 5));
            Outbound saved = new OutboundTestBuilder().build();
            ReflectionTestUtils.setField(saved, "id", 1L);
            given(outboundRepository.save(any(Outbound.class))).willReturn(saved);

            // when
            OutboundResponseDto response = outboundService.createOutbound(1L, 10L, OutboundType.ORDER, lines);

            // then
            assertThat(response.outboundType()).isEqualTo(OutboundType.ORDER);
            assertThat(response.outboundStatus()).isEqualTo(OutboundStatus.PENDING);
        }

        @Test
        void 출고_의뢰_등록_시_출고_유형이나_창고_정보가_누락되면_예외가_발생한다() {
            List<OutboundLine> lines = List.of(new OutboundLine(1L, 1L, 5));

            assertThatThrownBy(() -> outboundService.createOutbound(1L, null, OutboundType.ORDER, lines))
                    .isInstanceOf(OutboundWarehouseRequiredException.class);

            assertThatThrownBy(() -> outboundService.createOutbound(1L, 10L, null, lines))
                    .isInstanceOf(OutboundTypeRequiredException.class);
        }

        @Test
        void 출고_의뢰_등록_시_품목의_출고_요청_수량이_0_이하이면_예외가_발생한다() {
            // given
            List<OutboundLine> lines = List.of(new OutboundLine(1L, 1L, 0));

            // when & then
            assertThatThrownBy(() -> outboundService.createOutbound(1L, 10L, OutboundType.ORDER, lines))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(OutboundConstants.INVALID_TARGET_QUANTITY_MESSAGE);
        }
    }

    @Nested
    class 창고_재고_할당 {

        @Test
        void 출고_대기_상태인_전표의_품목들을_재고_할당_처리하면_재고_할당_상태로_정상_전환된다() {
            // given
            Long outboundId = 1L;
            Outbound outbound = new OutboundTestBuilder().build();
            given(outboundRepository.findByIdForUpdate(outboundId)).willReturn(Optional.of(outbound));

            // when
            OutboundResponseDto response = outboundService.allocateInventory(outboundId);

            // then
            assertThat(response.outboundStatus()).isEqualTo(OutboundStatus.ALLOCATED);
        }

        @Test
        void 재고_할당_성공_시_재고_도메인에_할당_요청을_전파하기_위한_재고_할당_이벤트가_발행된다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build()));

            // when
            outboundService.allocateInventory(outboundId);

            // then
            verify(eventPublisher, times(1)).publish(any(OutboundAllocatedEvent.class));
        }

        @Test
        void 이미_출고_준비_중이거나_출하_완료된_전표에_대해_중복으로_재고_할당을_시도하면_예외가_발생한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildProcessing()));

            // when & then
            assertThatThrownBy(() -> outboundService.allocateInventory(outboundId))
                    .isInstanceOf(OutboundAllocateNotAllowedException.class);
        }

        @Test
        void 재고_할당_프로세스_진입_시_동시성_방어를_위해_출고_전표에_비관적_락을_걸고_조회한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build()));

            // when
            outboundService.allocateInventory(outboundId);

            // then
            verify(outboundRepository, times(1)).findByIdForUpdate(outboundId);
            verify(outboundRepository, never()).findById(outboundId);
        }
    }

    @Nested
    class 출고_작업_준비 {

        @Test
        void 재고_할당이_완료된_전표는_현장_피킹_및_패킹_개시_시_출고_준비_중_상태로_정상_전환된다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildAllocated()));

            // when
            OutboundResponseDto response = outboundService.startProcessing(outboundId);

            // then
            assertThat(response.outboundStatus()).isEqualTo(OutboundStatus.PROCESSING);
        }

        @Test
        void 재고_할당_단계가_누락된_출고_대기_상태의_전표를_바로_출고_준비_중_상태로_만들려고_하면_예외가_발생한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build())); // PENDING

            // when & then
            assertThatThrownBy(() -> outboundService.startProcessing(outboundId))
                    .isInstanceOf(OutboundProcessingNotAllowedException.class);
        }
    }

    @Nested
    class 출하_완료 {

        @Test
        void 출고_준비_중인_전표를_최종_출하시키면_출하_완료_상태로_전환된다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildFullyPickedProcessing()));

            // when
            OutboundResponseDto response = outboundService.ship(outboundId);

            // then
            assertThat(response.outboundStatus()).isEqualTo(OutboundStatus.SHIPPED);
        }

        @Test
        void 출하_완료_성공_시_타_도메인_전파를_위한_출하_완료_이벤트가_정확히_1번_발행된다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildFullyPickedProcessing()));

            // when
            outboundService.ship(outboundId);

            // then
            verify(eventPublisher, times(1)).publish(any(OutboundShippedEvent.class));
        }

        @Test
        void 출고_준비_중_이외의_상태인_전표를_강제로_출하_완료_처리하려고_하면_예외가_발생한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build())); // PENDING

            // when & then
            assertThatThrownBy(() -> outboundService.ship(outboundId))
                    .isInstanceOf(OutboundShipNotAllowedException.class);
        }
    }

    @Nested
    class 출고_취소 {

        @Test
        void 출고_대기_또는_재고_할당_상태의_전표는_취소가_가능하며_출고_취소_상태로_정상_전환된다() {
            Long outboundId = 1L;

            // PENDING 취소
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build()));
            assertThat(outboundService.cancel(outboundId).outboundStatus())
                    .isEqualTo(OutboundStatus.CANCELED);

            // ALLOCATED 취소
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildAllocated()));
            assertThat(outboundService.cancel(outboundId).outboundStatus())
                    .isEqualTo(OutboundStatus.CANCELED);
        }

        @Test
        void 출고_대기_상태에서_취소_시_출고_취소_이벤트만_발행되고_재고_환원_이벤트는_발행되지_않는다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().build())); // PENDING

            // when
            outboundService.cancel(outboundId);

            // then
            verify(eventPublisher, times(1)).publish(any(OutboundCanceledEvent.class));
            verify(eventPublisher, never()).publish(any(OutboundStockReleaseRequestedEvent.class));
        }

        @Test
        void 재고_할당_상태에서_취소_시_출고_취소_이벤트와_재고_환원_요청_이벤트가_각각_1번씩_발행된다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildAllocated()));

            // when
            outboundService.cancel(outboundId);

            // then
            verify(eventPublisher, times(1)).publish(any(OutboundCanceledEvent.class));
            verify(eventPublisher, times(1)).publish(any(OutboundStockReleaseRequestedEvent.class));
        }

        @Test
        void 이미_현장_작업이_시작된_출고_준비_중_이상의_전표는_취소할_수_없고_예외가_발생한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildProcessing()));

            // when & then
            assertThatThrownBy(() -> outboundService.cancel(outboundId))
                    .isInstanceOf(OutboundCancelNotAllowedException.class);
        }

        @Test
        void 이미_출하_완료되어_창고를_떠난_전표에_대해_취소를_요청하면_예외가_발생한다() {
            // given
            Long outboundId = 1L;
            given(outboundRepository.findByIdForUpdate(outboundId))
                    .willReturn(Optional.of(new OutboundTestBuilder().buildShipped()));

            // when & then
            assertThatThrownBy(() -> outboundService.cancel(outboundId))
                    .isInstanceOf(OutboundCancelNotAllowedException.class);
        }
    }
}
