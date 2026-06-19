package com.kb.cosmetic_wms.domain.inbound.fixture;

import com.kb.cosmetic_wms.domain.inbound.InboundLine;
import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import com.kb.cosmetic_wms.domain.inbound.entity.InboundItem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InboundItemTestBuilder {

    private Long productId = 1L;
    private int quantity = 100;
    private final LocalDate manufactureDate = LocalDate.now().minusDays(10);
    private final LocalDate expirationDate = LocalDate.now().plusYears(2);
    private Long lotId = 100L;
    private Long sectionId = 200L;

    public InboundItemTestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public InboundItemTestBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InboundItemTestBuilder lotId(Long lotId) {
        this.lotId = lotId;
        return this;
    }

    public InboundItemTestBuilder sectionId(Long sectionId) {
        this.sectionId = sectionId;
        return this;
    }

    /**
     * WAITING 상태의 InboundItem 생성.
     * InboundItem 생성자가 package-private이므로 Inbound.addItem()을 통해 생성한다.
     */
    public InboundItem buildWaiting() {
        Inbound inbound = Inbound.create(LocalDateTime.now().plusDays(1), 1L, 1L);
        InboundLine line = new InboundLine(productId, quantity, manufactureDate, expirationDate);
        return inbound.addItem(line);
    }

    /** INSPECTING 상태의 InboundItem 생성 (로트·섹션 할당 완료) */
    public InboundItem buildInspecting() {
        InboundItem item = buildWaiting();
        item.completePutaway(lotId, sectionId);
        return item;
    }

    /** NORMAL 상태의 InboundItem 생성 (검수 정상 완료) */
    public InboundItem buildNormal() {
        InboundItem item = buildInspecting();
        item.changeToNormal();
        return item;
    }

    /** HOLD 상태의 InboundItem 생성 (검수 보류) */
    public InboundItem buildHold() {
        InboundItem item = buildInspecting();
        item.changeToHold();
        return item;
    }
}
