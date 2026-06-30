package com.kb.cosmetic_wms.putaway;

import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayAlreadyCompletedException;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayErrorCode;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;
import com.kb.cosmetic_wms.putaway.fixture.PutawayOrderTestBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutawayOrderEntityTest {

    @Test
    void 정상_수량으로_생성하면_PENDING_상태의_적재_지시서가_생성된다() {
        PutawayOrder order = new PutawayOrderTestBuilder().quantity(50).build();

        assertThat(order.getStatus()).isEqualTo(PutawayStatus.PENDING);
        assertThat(order.getQuantity()).isEqualTo(50);
        assertThat(order.getId()).isNull();
    }

    @Test
    void 수량이_0이면_INVALID_QUANTITY_예외를_던진다() {
        assertThatThrownBy(() -> new PutawayOrderTestBuilder().quantity(0).build())
                .hasMessage(PutawayErrorCode.INVALID_QUANTITY.getMessage());
    }

    @Test
    void 수량이_음수이면_INVALID_QUANTITY_예외를_던진다() {
        assertThatThrownBy(() -> new PutawayOrderTestBuilder().quantity(-1).build())
                .hasMessage(PutawayErrorCode.INVALID_QUANTITY.getMessage());
    }

    @Test
    void PENDING_상태에서_complete_호출하면_COMPLETED_상태로_전이된다() {
        PutawayOrder order = new PutawayOrderTestBuilder().build();

        order.complete();

        assertThat(order.getStatus()).isEqualTo(PutawayStatus.COMPLETED);
    }

    @Test
    void COMPLETED_상태에서_complete_를_재호출하면_ALREADY_COMPLETED_예외를_던진다() {
        PutawayOrder order = new PutawayOrderTestBuilder().buildCompleted();

        assertThatThrownBy(order::complete)
                .isInstanceOf(PutawayAlreadyCompletedException.class)
                .hasMessage(PutawayErrorCode.ALREADY_COMPLETED.getMessage());
    }
}
