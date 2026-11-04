package com.kb.cosmetic_wms.outbound;

import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.out.LotAllocationQueryPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.application.service.OutboundQueryService;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundQueryServiceTest {

    @InjectMocks
    private OutboundQueryService service;

    @Mock
    private LotAllocationQueryPort lotAllocationQueryPort;

    @Mock
    private OutboundPort outboundPort;

    @Test
    void 로트에_할당된_출고_중_재고를_점유한_진행_중_전표만_조회한다() {
        // given
        Outbound allocated = mock(Outbound.class);
        given(allocated.getId()).willReturn(101L);
        given(allocated.getOutboundStatus()).willReturn(OutboundStatus.ALLOCATED);
        given(lotAllocationQueryPort.findAllocatedOutboundIds(10L)).willReturn(List.of(101L, 102L));
        given(outboundPort.findByIdsAndStatuses(List.of(101L, 102L), Set.of(OutboundStatus.ALLOCATED, OutboundStatus.PROCESSING)))
                .willReturn(List.of(allocated));

        // when
        List<OutboundResult> results = service.findInProgressByLotId(10L);

        // then
        assertThat(results).extracting(OutboundResult::outboundId).containsExactly(101L);
    }

    @Test
    void 할당된_출고가_없으면_출고_전표를_조회하지_않고_빈_목록을_반환한다() {
        given(lotAllocationQueryPort.findAllocatedOutboundIds(10L)).willReturn(List.of());

        assertThat(service.findInProgressByLotId(10L)).isEmpty();
        verify(outboundPort, never()).findByIdsAndStatuses(any(), any());
    }

    @Test
    void 재고를_점유하는_출고_상태는_할당과_출고_준비_중이다() {
        assertThat(OutboundStatus.holdingAllocatedStock())
                .containsExactlyInAnyOrder(OutboundStatus.ALLOCATED, OutboundStatus.PROCESSING);
        assertThat(OutboundStatus.PENDING.holdsAllocatedStock()).isFalse();
        assertThat(OutboundStatus.SHIPPED.holdsAllocatedStock()).isFalse();
        assertThat(OutboundStatus.CANCELED.holdsAllocatedStock()).isFalse();
    }
}
