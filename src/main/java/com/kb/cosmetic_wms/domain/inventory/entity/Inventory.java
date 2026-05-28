package com.kb.cosmetic_wms.domain.inventory.entity;

import com.kb.cosmetic_wms.domain.inventory.enums.AllocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.LocStatus;
import com.kb.cosmetic_wms.domain.inventory.enums.QualityStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import com.kb.cosmetic_wms.domain.storage.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    private Warehouse warehouse;

    private int quantity;

    private int availableQuantity;

    @Enumerated(EnumType.STRING)
    private AllocStatus allocStatus;

    @Enumerated(EnumType.STRING)
    private QualityStatus qualityStatus;

    @Enumerated(EnumType.STRING)
    private LocStatus locStatus;

    private Inventory(Product product, Lot lot, Section section, Warehouse warehouse,
                      int quantity, int availableQuantity, AllocStatus allocStatus,
                      QualityStatus qualityStatus, LocStatus locStatus) {
        this.product = product;
        this.lot = lot;
        this.section = section;
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.allocStatus = allocStatus;
        this.qualityStatus = qualityStatus;
        this.locStatus = locStatus;
    }

    public static Inventory create(Product product, Lot lot, Section section, Warehouse warehouse,
                                   int quantity, int availableQuantity, AllocStatus allocStatus,
                                   QualityStatus qualityStatus, LocStatus locStatus) {

        validateQuantity(quantity);
        validateAvailableQuantity(quantity, availableQuantity);
        validateQualityAndAvailableQuantity(qualityStatus, availableQuantity);
        validateStatusCombination(allocStatus, qualityStatus, locStatus);

        return new Inventory(product, lot, section, warehouse, quantity, availableQuantity,
                allocStatus, qualityStatus, locStatus);
    }

    // 출고 할당 메서드 (UNALLOCATED -> ALLOCATED)
    public void allocate(int allocQuantity) {
        if (allocQuantity <= 0) {
            throw new IllegalArgumentException("할당할 수량은 0보다 커야 합니다.");
        }
        if (this.availableQuantity < allocQuantity) {
            throw new IllegalArgumentException("가용 재고가 부족하여 할당할 수 없습니다.");
        }

        AllocStatus nextAllocStatus = AllocStatus.ALLOCATED;
        validateStatusCombination(nextAllocStatus, this.qualityStatus, this.locStatus);

        this.allocStatus = nextAllocStatus;
        this.availableQuantity -= allocQuantity;
    }

    // 품질 상태 변경 (불량 발견, 검수 완료 등)
    public void changeQualityStatus(QualityStatus nextQualityStatus) {
        if (nextQualityStatus == null) {
            throw new IllegalArgumentException("변경할 품질 상태는 필수입니다.");
        }

        validateStatusCombination(this.allocStatus, nextQualityStatus, this.locStatus);

        this.qualityStatus = nextQualityStatus;

        if (!this.qualityStatus.isNormal()) {
            this.availableQuantity = 0;
        }
    }

    // 위치/이동 상태 변경
    public void changeLocStatus(LocStatus nextLocStatus) {
        if (nextLocStatus == null) {
            throw new IllegalArgumentException("변경할 위치 상태는 필수입니다.");
        }

        validateStatusCombination(this.allocStatus, this.qualityStatus, nextLocStatus);

        this.locStatus = nextLocStatus;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 음수일 수 없습니다.");
        }
    }

    private static void validateAvailableQuantity(int quantity, int availableQuantity) {
        if (quantity < availableQuantity) {
            throw new IllegalArgumentException("출고 가능 수량은 총 재고 수량을 초과할 수 없습니다.");
        }
    }

    private static void validateQualityAndAvailableQuantity(QualityStatus qualityStatus, int availableQuantity) {
        if (!qualityStatus.isNormal() && availableQuantity > 0) {
            throw new IllegalArgumentException(
                    String.format("품질 상태가 %s(%s)일 경우 출고 가능 수량은 0이어야 합니다.",
                            qualityStatus.name(), qualityStatus.getDescription()));
        }
    }

    private static void validateStatusCombination(
            AllocStatus allocStatus, QualityStatus qualityStatus, LocStatus locStatus
    ) {
        // 주문 처리 중(ALLOCATED, SHIPPED)인 재고는 무조건 NORMAL 품질이어야 한다.
        if (allocStatus != AllocStatus.UNALLOCATED && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException("할당 또는 출고 완료된 재고는 품질 상태가 정상이어야 합니다.");
        }

        // 창고 간 이동(MOVING)은 오직 아직 주문에 묶이지 않은 UNALLOCATED 상태일 때만 가능하다.
        if (locStatus == LocStatus.MOVING && allocStatus != AllocStatus.UNALLOCATED) {
            throw new IllegalArgumentException("이미 가맹점 주문 처리 중인 재고는 창고 간 이동(MOVING)을 할 수 없습니다.");
        }

        // 창고 간 이동(MOVING)을 하려면 품질이 반드시 NORMAL 이어야 한다.
        if (locStatus == LocStatus.MOVING && !qualityStatus.isNormal()) {
            throw new IllegalArgumentException(
                    String.format("품질 상태가 %s인 결함/검수 재고는 창고 간 이동(MOVING)이 불가능합니다.", qualityStatus.getDescription())
            );
        }
    }
}
