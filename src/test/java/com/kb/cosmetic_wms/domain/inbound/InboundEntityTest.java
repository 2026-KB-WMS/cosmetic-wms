package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InboundEntityTest {

    private final Long warehouseId = 1L;
    private final Long partnerId = 1L;

    @Test
    void 입고_엔티티는_필수_값이_모두_존재하면_SCHEDULED_상태로_성공적으로_생성된다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);


        // when
        Inbound inbound = Inbound.create(inboundDate, warehouseId, partnerId);

        // then
        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
    }

    @Test
    void 입고_예정일이_현재_날짜보다_과거이면_예외를_던진다() {
        // given
        LocalDateTime pastInboundDate = LocalDateTime.now().minusYears(1);

        // when & then
        assertThatThrownBy(() ->
                Inbound.create(pastInboundDate, warehouseId, partnerId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.PAST_INBOUND_DATE_MESSAGE);
    }

    @Test
    void 입고_생성_시_창고_객체가_누락되면_예외를_던진다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);

        // when & then
        assertThatThrownBy(() ->
                Inbound.create(inboundDate, null, partnerId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.WAREHOUSE_REQUIRED_MESSAGE);
    }

    @Test
    void 입고_예정_상태에서는_취소가_가능하다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Inbound inbound = Inbound.create(inboundDate, warehouseId, partnerId);

        // when
        inbound.cancel();

        // then
        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.CANCELED);
    }

    @Test
    void 이미_작업이_진행_중인_입고_건은_취소_시_예외를_던진다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Inbound inbound = Inbound.create(inboundDate, warehouseId, partnerId);

        // when
        inbound.startExecution();

        // when & then
        assertThatThrownBy(inbound::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 작업이 진행되었거나 완료된 입고 건은 취소할 수 없습니다.");
    }

    @Test
    void 입고_완료_처리는_입고_진행_상태에서만_가능해야_한다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Inbound inbound = Inbound.create(inboundDate, warehouseId, partnerId);

        // when & then
        assertThatThrownBy(inbound::completeExecution
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다.");
    }

    @Test
    void 모든_품목의_검수_및_적재가_완료되지_않은_상태에서_입고_완료를_시도하면_예외를_던진다() {
        // given
        Inbound inbound = Inbound.create(LocalDateTime.now().plusDays(1), 1L, 1L);
        InboundLine line = new InboundLine(1L, 100, LocalDateTime.now(), LocalDateTime.now().plusYears(3));
        InboundItem item = inbound.addItem(line);

        inbound.startExecution();

        assertThatThrownBy(inbound::completeExecution)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("아직 적재가 완료되지 않았거나 검수 중인 품목이 존재");
    }

    @Test
    void 모든_품목이_NORMAL_또는_HOLD_상태로_검수가_끝나면_정상적으로_입고_완료_처리된다() {
        // given
        Inbound inbound = Inbound.create(LocalDateTime.now().plusDays(1), 1L, 1L);
        InboundLine line1 = new InboundLine(1L, 50, LocalDateTime.now(), LocalDateTime.now().plusYears(3));
        InboundLine line2 = new InboundLine(2L, 30, LocalDateTime.now(), LocalDateTime.now().plusYears(3));

        InboundItem item1 = inbound.addItem(line1);
        InboundItem item2 = inbound.addItem(line2);

        inbound.startExecution();

        item1.completePutaway(100L, 10L);
        item1.changeToNormal();

        item2.completePutaway(101L, 10L);
        item2.changeToHold();

        // when
        inbound.completeExecution();

        // then
        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.COMPLETED);
    }
}
