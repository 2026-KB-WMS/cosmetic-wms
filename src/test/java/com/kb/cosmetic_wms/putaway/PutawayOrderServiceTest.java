package com.kb.cosmetic_wms.putaway;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.putaway.domain.event.PutawayCompletedEvent;
import com.kb.cosmetic_wms.putaway.application.port.in.CreatePutawayOrderCommand;
import com.kb.cosmetic_wms.putaway.application.port.in.PutawayOrderResult;
import com.kb.cosmetic_wms.putaway.application.port.out.PutawayOrderPort;
import com.kb.cosmetic_wms.putaway.application.port.out.StorageSectionQueryPort;
import com.kb.cosmetic_wms.putaway.application.service.PutawayOrderService;
import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayAlreadyCompletedException;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayOrderNotFoundException;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayTargetSectionNotFoundException;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;
import com.kb.cosmetic_wms.putaway.fixture.PutawayOrderTestBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PutawayOrderServiceTest {

    @InjectMocks
    private PutawayOrderService putawayOrderService;

    @Mock
    private PutawayOrderPort putawayOrderPort;

    @Mock
    private StorageSectionQueryPort storageSectionQueryPort;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private AuditorAware<Long> auditorProvider;

    // =========================================================
    // 적재 지시서 생성
    // =========================================================

    @Nested
    class 적재_지시서_생성 {

        @Test
        void 합격_수량만_있으면_STORAGE_목적지로_적재_지시서_1건을_생성한다() {
            given(storageSectionQueryPort.findDockingSectionId(1L, 100L)).willReturn(5L);
            given(storageSectionQueryPort.findAvailableStorageSectionId(1L, 100L)).willReturn(Optional.of(10L));

            PutawayOrder saved = new PutawayOrderTestBuilder().quantity(30).targetSectionId(10L).build();
            ReflectionTestUtils.setField(saved, "id", 1L);
            given(putawayOrderPort.save(any())).willReturn(saved);

            List<PutawayOrderResult> results = putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 30, 0));

            assertThat(results).hasSize(1);
            assertThat(results.get(0).targetSectionId()).isEqualTo(10L);
            assertThat(results.get(0).quantity()).isEqualTo(30);
            assertThat(results.get(0).status()).isEqualTo(PutawayStatus.PENDING);
        }

        @Test
        void 불합격_수량만_있으면_QUARANTINE_목적지로_적재_지시서_1건을_생성한다() {
            given(storageSectionQueryPort.findDockingSectionId(1L, 100L)).willReturn(5L);
            given(storageSectionQueryPort.findAvailableQuarantineSectionId(1L)).willReturn(Optional.of(20L));

            PutawayOrder saved = new PutawayOrderTestBuilder().quantity(10).targetSectionId(20L).build();
            ReflectionTestUtils.setField(saved, "id", 2L);
            given(putawayOrderPort.save(any())).willReturn(saved);

            List<PutawayOrderResult> results = putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 0, 10));

            assertThat(results).hasSize(1);
            assertThat(results.get(0).targetSectionId()).isEqualTo(20L);
        }

        @Test
        void 합격_불합격_수량이_모두_있으면_적재_지시서_2건을_생성한다() {
            given(storageSectionQueryPort.findDockingSectionId(1L, 100L)).willReturn(5L);
            given(storageSectionQueryPort.findAvailableStorageSectionId(1L, 100L)).willReturn(Optional.of(10L));
            given(storageSectionQueryPort.findAvailableQuarantineSectionId(1L)).willReturn(Optional.of(20L));

            PutawayOrder passedOrder = new PutawayOrderTestBuilder().quantity(30).targetSectionId(10L).build();
            PutawayOrder failedOrder = new PutawayOrderTestBuilder().quantity(10).targetSectionId(20L).build();
            ReflectionTestUtils.setField(passedOrder, "id", 1L);
            ReflectionTestUtils.setField(failedOrder, "id", 2L);
            given(putawayOrderPort.save(any()))
                    .willReturn(passedOrder)
                    .willReturn(failedOrder);

            List<PutawayOrderResult> results = putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 30, 10));

            assertThat(results).hasSize(2);

            ArgumentCaptor<PutawayOrder> captor = ArgumentCaptor.forClass(PutawayOrder.class);
            verify(putawayOrderPort, org.mockito.Mockito.times(2)).save(captor.capture());
            List<PutawayOrder> saved = captor.getAllValues();
            assertThat(saved.get(0).getQuantity()).isEqualTo(30);
            assertThat(saved.get(1).getQuantity()).isEqualTo(10);
        }

        @Test
        void 합격_수량이_있는데_보관_가능한_STORAGE_구역이_없으면_예외를_던진다() {
            given(storageSectionQueryPort.findDockingSectionId(1L, 100L)).willReturn(5L);
            given(storageSectionQueryPort.findAvailableStorageSectionId(1L, 100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 30, 0)))
                    .isInstanceOf(PutawayTargetSectionNotFoundException.class);
        }

        @Test
        void 불합격_수량이_있는데_보관_가능한_QUARANTINE_구역이_없으면_예외를_던진다() {
            given(storageSectionQueryPort.findDockingSectionId(1L, 100L)).willReturn(5L);
            given(storageSectionQueryPort.findAvailableQuarantineSectionId(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 0, 10)))
                    .isInstanceOf(PutawayTargetSectionNotFoundException.class);
        }

        @Test
        void 합격_불합격_수량이_모두_0이면_빈_리스트를_반환한다() {
            List<PutawayOrderResult> results = putawayOrderService.create(
                    new CreatePutawayOrderCommand(1L, 10L, 100L, 1L, 0, 0));

            assertThat(results).isEmpty();
        }
    }

    // =========================================================
    // 적재 완료 처리
    // =========================================================

    @Nested
    class 적재_완료_처리 {

        @Test
        void PENDING_상태_지시서를_완료_처리하면_COMPLETED_상태와_이벤트를_발행한다() {
            PutawayOrder pending = new PutawayOrderTestBuilder().quantity(50).normalQuality(true).build();
            ReflectionTestUtils.setField(pending, "id", 1L);
            given(auditorProvider.getCurrentAuditor()).willReturn(Optional.of(99L));
            given(putawayOrderPort.findByIdForUpdate(1L)).willReturn(Optional.of(pending));
            given(putawayOrderPort.save(any())).willAnswer(inv -> inv.getArgument(0));

            PutawayOrderResult result = putawayOrderService.complete(1L);

            assertThat(result.status()).isEqualTo(PutawayStatus.COMPLETED);

            ArgumentCaptor<PutawayCompletedEvent> eventCaptor = ArgumentCaptor.forClass(PutawayCompletedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());
            PutawayCompletedEvent event = eventCaptor.getValue();
            assertThat(event.quantity()).isEqualTo(50);
            assertThat(event.normalQuality()).isTrue();
            assertThat(event.memberId()).isEqualTo(99L);
        }

        @Test
        void 존재하지_않는_지시서_완료_시_PutawayOrderNotFoundException이_발생한다() {
            given(auditorProvider.getCurrentAuditor()).willReturn(Optional.of(99L));
            given(putawayOrderPort.findByIdForUpdate(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> putawayOrderService.complete(999L))
                    .isInstanceOf(PutawayOrderNotFoundException.class);
        }

        @Test
        void 이미_완료된_지시서를_재완료하면_PutawayAlreadyCompletedException이_발생한다() {
            PutawayOrder completed = new PutawayOrderTestBuilder().buildCompleted();
            ReflectionTestUtils.setField(completed, "id", 1L);
            given(auditorProvider.getCurrentAuditor()).willReturn(Optional.of(99L));
            given(putawayOrderPort.findByIdForUpdate(1L)).willReturn(Optional.of(completed));

            assertThatThrownBy(() -> putawayOrderService.complete(1L))
                    .isInstanceOf(PutawayAlreadyCompletedException.class);
        }
    }

    // =========================================================
    // 적재 지시서 조회
    // =========================================================

    @Nested
    class 적재_지시서_조회 {

        @Test
        void 존재하는_ID로_조회하면_적재_지시서_정보를_반환한다() {
            PutawayOrder order = new PutawayOrderTestBuilder().quantity(50).build();
            ReflectionTestUtils.setField(order, "id", 1L);
            given(putawayOrderPort.findById(1L)).willReturn(Optional.of(order));

            PutawayOrderResult result = putawayOrderService.findById(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.quantity()).isEqualTo(50);
            assertThat(result.status()).isEqualTo(PutawayStatus.PENDING);
        }

        @Test
        void 존재하지_않는_ID로_조회하면_PutawayOrderNotFoundException이_발생한다() {
            given(putawayOrderPort.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> putawayOrderService.findById(999L))
                    .isInstanceOf(PutawayOrderNotFoundException.class);
        }
    }
}
