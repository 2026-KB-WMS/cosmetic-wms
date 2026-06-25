package com.kb.cosmetic_wms.inbound.fixture;

import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.model.Inbound;
import com.kb.cosmetic_wms.inbound.domain.model.InboundItem;
import com.kb.cosmetic_wms.inbound.domain.model.InboundLine;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InboundItemTestBuilder {

    private Long id = 1L;
    private Long productId = 1L;
    private int quantity = 100;
    private LocalDate manufactureDate = LocalDate.now().minusDays(10);
    private LocalDate expirationDate = LocalDate.now().plusYears(2);
    private InspectionStatus inspectionStatus = InspectionStatus.WAITING;
    private Long lotId = null;
    private Long sectionId = null;

    public InboundItemTestBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public InboundItemTestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public InboundItemTestBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InboundItemTestBuilder inspectionStatus(InspectionStatus status) {
        this.inspectionStatus = status;
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
     * reconstitute 기반 빌드 (서비스 테스트용 - ID 있음)
     */
    public InboundItem build() {
        return InboundItem.reconstitute(id, productId, quantity, manufactureDate, expirationDate,
                inspectionStatus, lotId, sectionId);
    }

    /**
     * 도메인 플로우 기반 빌드 (엔티티 테스트용 - ID 없음, WAITING 상태)
     */
    public InboundItem buildWaiting() {
        Inbound inbound = Inbound.create(LocalDateTime.now().plusDays(1), 1L, 1L);
        return inbound.addItem(new InboundLine(productId, quantity, manufactureDate, expirationDate));
    }

    /**
     * 도메인 플로우 기반 빌드 (엔티티 테스트용 - INSPECTING 상태)
     */
    public InboundItem buildInspecting() {
        InboundItem item = buildWaiting();
        item.completePutaway(lotId != null ? lotId : 100L, sectionId != null ? sectionId : 200L);
        return item;
    }

    /**
     * 도메인 플로우 기반 빌드 (엔티티 테스트용 - NORMAL 상태)
     */
    public InboundItem buildNormal() {
        InboundItem item = buildInspecting();
        item.changeToNormal();
        return item;
    }

    /**
     * 도메인 플로우 기반 빌드 (엔티티 테스트용 - HOLD 상태)
     */
    public InboundItem buildHold() {
        InboundItem item = buildInspecting();
        item.changeToHold();
        return item;
    }
}
