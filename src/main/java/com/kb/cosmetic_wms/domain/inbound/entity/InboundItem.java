package com.kb.cosmetic_wms.domain.inbound.entity;

import com.kb.cosmetic_wms.domain.inbound.InboundLine;
import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InboundItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private LocalDateTime manufactureDate;
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private InspectionStatus inspectionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Inbound inbound;

    private Long productId;
    private Long lotId;
    private Long sectionId;

    InboundItem(Inbound inbound, InboundLine line) {
        validateInbound(inbound);
        validateQuantity(line.quantity());
        validateProductId(line.productId());

        this.inbound = inbound;
        this.productId = line.productId();
        this.quantity = line.quantity();
        this.manufactureDate = line.manufactureDate();
        this.expirationDate = line.expirationDate();
        this.inspectionStatus = InspectionStatus.WAITING;
    }

    /**
     * 현장 실물 적재(Putaway) 완료 처리
     * <p>검수 대기(WAITING) 상태의 품목에 생성된 로트와 보관 섹션을 할당하며 검수 중(INSPECTING) 상태로 전이합니다.</p>
     *
     * @param lotId     현장에서 발행된 재고 식별 로트 ID
     * @param sectionId 상품이 적재된 창고 구역(섹션) ID
     * @throws IllegalStateException    이미 적재가 완료되었거나 검수가 진행된 품목인 경우
     * @throws IllegalArgumentException 로트 ID 또는 섹션 ID가 누락된 경우
     */
    public void completePutaway(Long lotId, Long sectionId) {
        validatePutawayTarget();
        validatePutawayFields(lotId, sectionId);

        this.lotId = lotId;
        this.sectionId = sectionId;
        this.inspectionStatus = InspectionStatus.INSPECTING;
    }

    /**
     * 품질 판정 - 정상 완료 처리 (INSPECTING -> NORMAL)
     * <p>검수 결과 물성에 이상이 없는 경우 정상 재고 상태로 변경합니다.</p>
     *
     * @throws IllegalStateException 검수 중(INSPECTING) 상태가 아닌 경우
     */
    public void changeToNormal() {
        if (this.inspectionStatus != InspectionStatus.INSPECTING) {
            throw new IllegalStateException(InboundConstants.INVALID_NORMAL_STATUS_MESSAGE);
        }
        this.inspectionStatus = InspectionStatus.NORMAL;
    }

    /**
     * 품질 판정 - 검수 보류 처리 (INSPECTING -> HOLD)
     * <p>파손, 수량 불일치 등 현장 조치나 추가 검사가 필요한 경우 보류 재고 상태로 변경합니다.</p>
     *
     * @throws IllegalStateException 검수 중(INSPECTING) 상태가 아닌 경우
     */
    public void changeToHold() {
        if (this.inspectionStatus != InspectionStatus.INSPECTING) {
            throw new IllegalStateException(InboundConstants.INVALID_HOLD_STATUS_MESSAGE);
        }
        this.inspectionStatus = InspectionStatus.HOLD;
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE);
        }
    }

    private static void validateInbound(Inbound inbound) {
        if (inbound == null) {
            throw new IllegalArgumentException(InboundConstants.INBOUND_MASTER_REQUIRED_MESSAGE);
        }
    }

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(InboundConstants.INBOUND_PRODUCT_REQUIRED_MESSAGE);
        }
    }

    private void validatePutawayTarget() {
        if (this.inspectionStatus != InspectionStatus.WAITING) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_PUTAWAY_STATUS_MESSAGE,
                            this.inspectionStatus.getDescription())
            );
        }
    }

    private void validatePutawayFields(Long lotId, Long sectionId) {
        if (lotId == null) {
            throw new IllegalArgumentException(InboundConstants.PUTAWAY_LOT_REQUIRED_MESSAGE);
        }
        if (sectionId == null) {
            throw new IllegalArgumentException(InboundConstants.PUTAWAY_SECTION_REQUIRED_MESSAGE);
        }
    }
}
