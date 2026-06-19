package com.kb.cosmetic_wms.domain.inventory;

import com.kb.cosmetic_wms.domain.inbound.event.InboundCompletedEvent;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inventory.dto.InboundPutawayCommand;
import com.kb.cosmetic_wms.domain.inventory.event.InventoryEventHandler;
import com.kb.cosmetic_wms.domain.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.AuditorAware;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryEventHandlerTest {

    @InjectMocks
    private InventoryEventHandler handler;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private AuditorAware<Long> auditorProvider;

    @BeforeEach
    void setUp() {
        given(auditorProvider.getCurrentAuditor()).willReturn(Optional.of(1L));
    }

    // =========================================================
    // 품목 필터링
    // =========================================================

    @Nested
    class 품목_필터링 {

        @Test
        void NORMAL_품목만_재고_생성_대상이_된다() {
            // given
            InboundCompletedEvent event = new InboundCompletedEvent(
                    10L, 1L,
                    List.of(
                            new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.NORMAL),
                            new InboundCompletedEvent.ItemSnapshot(101L, 201L, 301L, 30, InspectionStatus.HOLD)
                    )
            );

            // when
            handler.onInboundCompleted(event);

            // then
            verify(inventoryService).createFromInbound(
                    new InboundPutawayCommand(100L, 200L, 300L, 1L, 50, 10L, 1L));
            verify(inventoryService, never()).createFromInbound(
                    new InboundPutawayCommand(101L, 201L, 301L, 1L, 30, 10L, 1L));
        }

        @Test
        void HOLD_품목만_존재하면_재고가_생성되지_않는다() {
            // given
            InboundCompletedEvent event = new InboundCompletedEvent(
                    10L, 1L,
                    List.of(
                            new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.HOLD),
                            new InboundCompletedEvent.ItemSnapshot(101L, 201L, 301L, 30, InspectionStatus.HOLD)
                    )
            );

            // when
            handler.onInboundCompleted(event);

            // then
            verify(inventoryService, never()).createFromInbound(any(InboundPutawayCommand.class));
        }

        @Test
        void NORMAL_품목이_복수이면_각각_재고가_생성된다() {
            // given
            InboundCompletedEvent event = new InboundCompletedEvent(
                    10L, 1L,
                    List.of(
                            new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.NORMAL),
                            new InboundCompletedEvent.ItemSnapshot(101L, 201L, 301L, 30, InspectionStatus.NORMAL),
                            new InboundCompletedEvent.ItemSnapshot(102L, 202L, 302L, 20, InspectionStatus.HOLD)
                    )
            );

            // when
            handler.onInboundCompleted(event);

            // then — NORMAL 2개만 호출
            verify(inventoryService, times(2)).createFromInbound(any(InboundPutawayCommand.class));
            verify(inventoryService).createFromInbound(new InboundPutawayCommand(100L, 200L, 300L, 1L, 50, 10L, 1L));
            verify(inventoryService).createFromInbound(new InboundPutawayCommand(101L, 201L, 301L, 1L, 30, 10L, 1L));
        }
    }

    // =========================================================
    // 이벤트 데이터 전달
    // =========================================================

    @Nested
    class 이벤트_데이터_전달 {

        @Test
        void AuditorAware에서_조회한_memberId로_재고를_생성한다() {
            // given
            Long memberId = 42L;
            given(auditorProvider.getCurrentAuditor()).willReturn(Optional.of(memberId));

            InboundCompletedEvent event = new InboundCompletedEvent(
                    10L, 1L,
                    List.of(new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.NORMAL))
            );

            // when
            handler.onInboundCompleted(event);

            // then
            verify(inventoryService).createFromInbound(new InboundPutawayCommand(100L, 200L, 300L, 1L, 50, 10L, memberId));
        }

        @Test
        void warehouseId가_모든_품목의_재고_생성에_공유된다() {
            // given
            Long warehouseId = 99L;
            InboundCompletedEvent event = new InboundCompletedEvent(
                    10L, warehouseId,
                    List.of(
                            new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.NORMAL),
                            new InboundCompletedEvent.ItemSnapshot(101L, 201L, 301L, 30, InspectionStatus.NORMAL)
                    )
            );

            // when
            handler.onInboundCompleted(event);

            // then
            verify(inventoryService).createFromInbound(new InboundPutawayCommand(100L, 200L, 300L, warehouseId, 50, 10L, 1L));
            verify(inventoryService).createFromInbound(new InboundPutawayCommand(101L, 201L, 301L, warehouseId, 30, 10L, 1L));
        }

        @Test
        void inboundId가_모든_품목의_referenceId로_전달된다() {
            // given
            Long inboundId = 777L;
            InboundCompletedEvent event = new InboundCompletedEvent(
                    inboundId, 1L,
                    List.of(
                            new InboundCompletedEvent.ItemSnapshot(100L, 200L, 300L, 50, InspectionStatus.NORMAL),
                            new InboundCompletedEvent.ItemSnapshot(101L, 201L, 301L, 30, InspectionStatus.NORMAL)
                    )
            );

            // when
            handler.onInboundCompleted(event);

            // then
            verify(inventoryService).createFromInbound(
                    argThat(cmd -> cmd.inboundId().equals(inboundId) && cmd.quantity() == 50));
            verify(inventoryService).createFromInbound(
                    argThat(cmd -> cmd.inboundId().equals(inboundId) && cmd.quantity() == 30));
        }
    }
}
