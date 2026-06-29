package com.kb.cosmetic_wms.inbound;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;
import com.kb.cosmetic_wms.inbound.domain.model.ReceiveLineData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InboundEntityTest {

    private static final Long WAREHOUSE_ID = 1L;
    private static final Long PARTNER_ID = 1L;
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusDays(1);

    private List<InboundLine> defaultLines() {
        return List.of(InboundLine.create(1L, 100));
    }

    @Test
    void 필수_값이_모두_존재하면_SCHEDULED_상태로_입고_전표가_생성된다() {
        Inbound inbound = Inbound.create(FUTURE_DATE, WAREHOUSE_ID, PARTNER_ID, defaultLines());

        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
        assertThat(inbound.getInboundLines()).hasSize(1);
    }

    @Test
    void 품목_라인이_없으면_InboundLinesRequiredException이_발생한다() {
        assertThatThrownBy(() -> Inbound.create(FUTURE_DATE, WAREHOUSE_ID, PARTNER_ID, List.of()))
                .isInstanceOf(InboundLinesRequiredException.class)
                .hasMessage(InboundErrorCode.INBOUND_LINES_REQUIRED.getMessage());
    }

    @Test
    void 입고_예정일이_현재_날짜보다_과거이면_예외를_던진다() {
        assertThatThrownBy(() ->
                Inbound.create(LocalDateTime.now().minusDays(1), WAREHOUSE_ID, PARTNER_ID, defaultLines()))
                .isInstanceOf(InboundPastDateException.class)
                .hasMessage(InboundErrorCode.INBOUND_PAST_DATE.getMessage());
    }

    @Test
    void 창고_ID가_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> Inbound.create(FUTURE_DATE, null, PARTNER_ID, defaultLines()))
                .isInstanceOf(InboundWarehouseIdRequiredException.class)
                .hasMessage(InboundErrorCode.INBOUND_WAREHOUSE_ID_REQUIRED.getMessage());
    }

    @Test
    void SCHEDULED_상태에서_수령_확인하면_RECEIVED로_전환된다() {
        Inbound persisted = Inbound.reconstitute(1L, InboundStatus.SCHEDULED, FUTURE_DATE,
                WAREHOUSE_ID, PARTNER_ID,
                List.of(InboundLine.reconstitute(1L, 1L, 100, 0, null, null, null)));

        persisted.receive(Map.of(1L, new ReceiveLineData(95, "LOT0001",
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusYears(3))));

        assertThat(persisted.getInboundStatus()).isEqualTo(InboundStatus.RECEIVED);
        assertThat(persisted.getInboundLines().get(0).getReceivedQuantity()).isEqualTo(95);
        assertThat(persisted.getInboundLines().get(0).getManufacturerLotNumber()).isEqualTo("LOT0001");
    }

    @Test
    void 수령_확인_요청의_라인_ID가_입고_전표와_일치하지_않으면_InboundReceiveLineMismatchException이_발생한다() {
        Inbound inbound = Inbound.reconstitute(1L, InboundStatus.SCHEDULED, FUTURE_DATE,
                WAREHOUSE_ID, PARTNER_ID,
                List.of(InboundLine.reconstitute(1L, 1L, 100, 0, null, null, null)));

        assertThatThrownBy(() -> inbound.receive(Map.of()))
                .isInstanceOf(InboundReceiveLineMismatchException.class)
                .hasMessage(InboundErrorCode.INBOUND_RECEIVE_LINE_MISMATCH.getMessage());
    }

    @Test
    void RECEIVED_상태에서_수령_확인을_다시_시도하면_InboundInvalidReceiveStatusException이_발생한다() {
        Inbound received = Inbound.reconstitute(1L, InboundStatus.RECEIVED, FUTURE_DATE,
                WAREHOUSE_ID, PARTNER_ID, defaultLines());

        assertThatThrownBy(() -> received.receive(Map.of()))
                .isInstanceOf(InboundInvalidReceiveStatusException.class)
                .hasMessageContaining("입고 예정(SCHEDULED) 상태에서만 수령 확인이 가능합니다");
    }

    @Test
    void SCHEDULED_상태에서_취소하면_CANCELED로_전환된다() {
        Inbound inbound = Inbound.create(FUTURE_DATE, WAREHOUSE_ID, PARTNER_ID, defaultLines());

        inbound.cancel();

        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.CANCELED);
    }

    @Test
    void RECEIVED_상태에서_취소를_시도하면_InboundInvalidCancelStatusException이_발생한다() {
        Inbound received = Inbound.reconstitute(1L, InboundStatus.RECEIVED, FUTURE_DATE,
                WAREHOUSE_ID, PARTNER_ID, defaultLines());

        assertThatThrownBy(received::cancel)
                .isInstanceOf(InboundInvalidCancelStatusException.class)
                .hasMessageContaining("이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다");
    }
}
