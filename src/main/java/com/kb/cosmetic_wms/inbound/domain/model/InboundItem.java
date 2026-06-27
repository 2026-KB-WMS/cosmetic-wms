package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InboundItem {

    private Long id;
    private Long productId;
    private InboundQuantity quantity;
    private LotDateRange lotDateRange;
    private InspectionStatus inspectionStatus;
    private Long lotId;
    private Long sectionId;

    InboundItem(InboundLine line) {
        validateProductId(line.productId());
        this.productId = line.productId();
        this.quantity = new InboundQuantity(line.quantity());
        this.lotDateRange = new LotDateRange(line.manufactureDate(), line.expirationDate());
        this.inspectionStatus = InspectionStatus.WAITING;
    }

    public static InboundItem reconstitute(Long id, Long productId, int quantity,
                                           LocalDate manufactureDate, LocalDate expirationDate,
                                           InspectionStatus inspectionStatus, Long lotId, Long sectionId) {
        InboundItem item = new InboundItem();
        item.id = id;
        item.productId = productId;
        item.quantity = new InboundQuantity(quantity);
        item.lotDateRange = new LotDateRange(manufactureDate, expirationDate);
        item.inspectionStatus = inspectionStatus;
        item.lotId = lotId;
        item.sectionId = sectionId;
        return item;
    }

    private InboundItem() {
    }

    public int getQuantity() {
        return quantity.value();
    }

    public LocalDate getManufactureDate() {
        return lotDateRange.manufactureDate();
    }

    public LocalDate getExpirationDate() {
        return lotDateRange.expirationDate();
    }

    public void completePutaway(Long lotId, Long sectionId) {
        validatePutawayTarget();
        validatePutawayFields(lotId, sectionId);
        this.lotId = lotId;
        this.sectionId = sectionId;
        this.inspectionStatus = InspectionStatus.INSPECTING;
    }

    public void changeToNormal() {
        if (!this.inspectionStatus.canApprove()) {
            throw new InboundInvalidApproveStatusException();
        }
        this.inspectionStatus = InspectionStatus.NORMAL;
    }

    public void changeToHold() {
        if (!this.inspectionStatus.canHold()) {
            throw new InboundInvalidHoldStatusException();
        }
        this.inspectionStatus = InspectionStatus.HOLD;
    }

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new InboundItemProductRequiredException();
        }
    }

    private void validatePutawayTarget() {
        if (!this.inspectionStatus.canPutaway()) {
            throw new InboundInvalidPutawayStatusException(this.inspectionStatus.getDescription());
        }
    }

    private void validatePutawayFields(Long lotId, Long sectionId) {
        if (lotId == null) {
            throw new InboundPutawayLotRequiredException();
        }
        if (sectionId == null) {
            throw new InboundPutawaySectionRequiredException();
        }
    }
}