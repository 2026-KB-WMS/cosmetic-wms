package com.kb.cosmetic_wms.domain.inbound.entity;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private int quantity;

    private LocalDateTime manufactureDate;

    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private InspectionStatus inspectionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Inbound inbound;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Section section;

    private InboundItem(Inbound inbound, Product product, int quantity,
                        LocalDateTime manufactureDate, LocalDateTime expirationDate
    ) {
        this.inbound = inbound;
        this.product = product;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
        this.inspectionStatus = InspectionStatus.WAITING;
    }

    public static InboundItem create(Inbound inbound, Product product, int quantity,
                                     LocalDateTime manufactureDate, LocalDateTime expirationDate
    ) {
        validateQuantity(quantity);
        validateRequiredFields(inbound, product);
        return new InboundItem(inbound, product, quantity, manufactureDate, expirationDate);
    }

    public void completePutaway(Lot lot, Section section) {
        validatePutawayTarget();
        validatePutawayFields(lot, section);

        this.lot = lot;
        this.section = section;

        this.inspectionStatus = InspectionStatus.INSPECTING;
    }

    public void changeToNormal() {
        if (this.inspectionStatus != InspectionStatus.INSPECTING) {
            throw new IllegalStateException(InboundConstants.INVALID_NORMAL_STATUS_MESSAGE);
        }
        this.inspectionStatus = InspectionStatus.NORMAL;
    }

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

    private static void validateRequiredFields(Inbound inbound, Product product) {
        if (inbound == null) {
            throw new IllegalArgumentException(InboundConstants.INBOUND_MASTER_REQUIRED_MESSAGE);
        }
        if (product == null) {
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

    private void validatePutawayFields(Lot lot, Section section) {
        if (lot == null) {
            throw new IllegalArgumentException(InboundConstants.PUTAWAY_LOT_REQUIRED_MESSAGE);
        }
        if (section == null) {
            throw new IllegalArgumentException(InboundConstants.PUTAWAY_SECTION_REQUIRED_MESSAGE);
        }
    }
}
