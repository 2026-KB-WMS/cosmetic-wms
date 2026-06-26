package com.kb.cosmetic_wms.outbound.domain.enums;

import com.kb.cosmetic_wms.outbound.domain.exception.OutboundAllocateNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundCancelNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundPickingNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundProcessingNotAllowedException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundShipNotAllowedException;
import lombok.Getter;

@Getter
public enum OutboundStatus {

    PENDING("출고 대기"),
    ALLOCATED("재고 할당"),
    PROCESSING("출고 준비 중"),
    SHIPPED("출하 완료"),
    CANCELED("출고 취소");

    private final String description;

    OutboundStatus(String description) {
        this.description = description;
    }

    public void validateAllocate() {
        if (this != PENDING) throw new OutboundAllocateNotAllowedException();
    }

    public void validateStartProcessing() {
        if (this != ALLOCATED) throw new OutboundProcessingNotAllowedException();
    }

    public void validateShip() {
        if (this != PROCESSING) throw new OutboundShipNotAllowedException();
    }

    public void validatePicking() {
        if (this != PROCESSING) throw new OutboundPickingNotAllowedException();
    }

    public void validateCancel() {
        if (this != PENDING && this != ALLOCATED) throw new OutboundCancelNotAllowedException();
    }
}
