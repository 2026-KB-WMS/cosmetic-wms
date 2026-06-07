package com.kb.cosmetic_wms.domain.inbound;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.enums.PartnerType;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import com.kb.cosmetic_wms.domain.storage.fixture.WarehouseTestBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InboundEntityTest {

    @Test
    void 입고_엔티티는_필수_값이_모두_존재하면_SCHEDULED_상태로_성공적으로_생성된다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");

        // when
        Inbound inbound = Inbound.create(inboundDate, warehouse, partner);

        // then
        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.SCHEDULED);
    }

    @Test
    void 입고_예정일이_현재_날짜보다_과거이면_예외를_던진다() {
        // given
        LocalDateTime pastInboundDate = LocalDateTime.now().minusYears(1);
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");

        // when & then
        assertThatThrownBy(() ->
                Inbound.create(pastInboundDate, warehouse, partner)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.PAST_INBOUND_DATE_MESSAGE);
    }

    @Test
    void 입고_생성_시_창고_객체가_누락되면_예외를_던진다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");

        // when & then
        assertThatThrownBy(() ->
                Inbound.create(inboundDate, null, partner)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(InboundConstants.WAREHOUSE_REQUIRED_MESSAGE);
    }

    @Test
    void 입고_예정_상태에서는_취소가_가능하다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");
        Inbound inbound = Inbound.create(inboundDate, warehouse, partner);

        // when
        inbound.cancel();

        // then
        assertThat(inbound.getInboundStatus()).isEqualTo(InboundStatus.CANCELED);
    }

    @Test
    void 이미_작업이_진행_중인_입고_건은_취소_시_예외를_던진다() {
        // given
        LocalDateTime inboundDate = LocalDateTime.now().plusYears(1);
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");
        Inbound inbound = Inbound.create(inboundDate, warehouse, partner);

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
        Warehouse warehouse = new WarehouseTestBuilder().build();
        Partner partner = Partner.create("본사", PartnerType.HEADQUARTER, "123-45-67890");
        Inbound inbound = Inbound.create(inboundDate, warehouse, partner);

        // when & then
        assertThatThrownBy(inbound::completeExecution
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("작업이 진행 중인 상태에서만 입고 완료 처리가 가능합니다.");
    }
}
