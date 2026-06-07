package com.kb.cosmetic_wms.domain.inbound.entity;

import com.kb.cosmetic_wms.domain.inbound.InboundLine;
import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.enums.InboundStatus;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InboundStatus inboundStatus;

    private LocalDateTime inboundDate;
    private Long warehouseId;
    private Long partnerId;

    @OneToMany(mappedBy = "inbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<InboundItem> inboundItems = new ArrayList<>();

    private Inbound(LocalDateTime inboundDate, Long warehouseId, Long partnerId) {
        this.inboundDate = inboundDate;
        this.warehouseId = warehouseId;
        this.partnerId = partnerId;
        this.inboundStatus = InboundStatus.SCHEDULED;
    }

    public static Inbound create(LocalDateTime inboundDate, Long warehouseId, Long partnerId) {
        validateInboundDate(inboundDate);
        validateWarehouseId(warehouseId);
        validatePartnerId(partnerId);

        return new Inbound(inboundDate, warehouseId, partnerId);
    }

    /**
     * 입고 상세 항목 추가
     * <p>입고 예정(SCHEDULED) 상태일 때만 품목 추가가 가능합니다.</p>
     *
     * @param line 입고 라인 명세 정보
     * @return 생성 및 추가된 InboundItem 엔티티
     * @throws IllegalStateException 입고 예정 상태가 아닐 경우
     */
    public InboundItem addItem(InboundLine line) {
        if (this.inboundStatus != InboundStatus.SCHEDULED) {
            throw new IllegalStateException(InboundConstants.INVALID_ADD_ITEM_MESSAGE);
        }

        InboundItem item = new InboundItem(this, line);
        this.inboundItems.add(item);

        return item;
    }

    /**
     * 입고 작업 시작 (SCHEDULED -> IN_PROGRESS)
     * <p>실물 입고 처리를 위해 현장 작업을 착수하는 단계입니다.</p>
     *
     * @throws IllegalStateException 입고 예정 상태가 아닐 경우
     */
    public void startExecution() {
        if (this.inboundStatus != InboundStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_START_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }
        this.inboundStatus = InboundStatus.IN_PROGRESS;
    }

    /**
     * 실물 적재 및 입고 완료 처리 (IN_PROGRESS -> COMPLETED)
     * <p>현장 적재 및 품질 검사(NORMAL / HOLD)가 모두 끝나 입고 전표를 최종 확정하는 단계입니다.</p>
     *
     * @throws IllegalStateException 작업 진행 중 상태가 아닐 경우 또는 미완료된 입고 품목이 존재할 경우
     */
    public void completeExecution() {
        if (this.inboundStatus != InboundStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_COMPLETE_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }

        boolean isAllInspected = this.inboundItems.stream()
                .allMatch(item -> item.getInspectionStatus() == InspectionStatus.NORMAL
                        || item.getInspectionStatus() == InspectionStatus.HOLD);

        if (!isAllInspected) {
            throw new IllegalStateException(InboundConstants.INCOMPLETE_INSPECTION_MESSAGE);
        }

        this.inboundStatus = InboundStatus.COMPLETED;
    }

    /**
     * 입고 계획 취소 (SCHEDULED -> CANCELED)
     * <p>현장 작업이 시작되기 전, 입고 전표를 취소 상태로 종결 처리합니다.</p>
     *
     * @throws IllegalStateException 입고 예정 상태가 아닐 경우
     */
    public void cancel() {
        if (this.inboundStatus != InboundStatus.SCHEDULED) {
            throw new IllegalStateException(
                    String.format(InboundConstants.INVALID_CANCEL_STATUS_MESSAGE,
                            this.inboundStatus.getDescription())
            );
        }
        this.inboundStatus = InboundStatus.CANCELED;
    }

    private static void validateInboundDate(LocalDateTime inboundDate) {
        if (inboundDate == null) {
            throw new IllegalArgumentException(InboundConstants.DATE_REQUIRED_MESSAGE);
        }
        if (inboundDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(InboundConstants.PAST_INBOUND_DATE_MESSAGE);
        }
    }

    private static void validateWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException(InboundConstants.WAREHOUSE_REQUIRED_MESSAGE);
        }
    }

    private static void validatePartnerId(Long partnerId) {
        if (partnerId == null) {
            throw new IllegalArgumentException(InboundConstants.PARTNER_REQUIRED_MESSAGE);
        }
    }
}
