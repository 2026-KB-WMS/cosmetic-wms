package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.constants.InboundConstants;
import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InboundItem {

    private Long id;
    private Long productId;
    private int quantity;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private InspectionStatus inspectionStatus;
    private Long lotId;
    private Long sectionId;

    InboundItem(InboundLine line) {
        validateQuantity(line.quantity());
        validateProductId(line.productId());

        this.productId = line.productId();
        this.quantity = line.quantity();
        this.manufactureDate = line.manufactureDate();
        this.expirationDate = line.expirationDate();
        this.inspectionStatus = InspectionStatus.WAITING;
    }

    public static InboundItem reconstitute(Long id, Long productId, int quantity,
                                           LocalDate manufactureDate, LocalDate expirationDate,
                                           InspectionStatus inspectionStatus, Long lotId, Long sectionId) {
        InboundItem item = new InboundItem();
        item.id = id;
        item.productId = productId;
        item.quantity = quantity;
        item.manufactureDate = manufactureDate;
        item.expirationDate = expirationDate;
        item.inspectionStatus = inspectionStatus;
        item.lotId = lotId;
        item.sectionId = sectionId;
        return item;
    }

    private InboundItem() {
    }

    public void completePutaway(Long lotId, Long sectionId) {
        validatePutawayTarget();
        validatePutawayFields(lotId, sectionId);
        this.lotId = lotId;
        this.sectionId = sectionId;
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
