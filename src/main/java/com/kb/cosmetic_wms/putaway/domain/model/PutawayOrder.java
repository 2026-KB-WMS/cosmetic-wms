package com.kb.cosmetic_wms.putaway.domain.model;

import com.kb.cosmetic_wms.putaway.domain.enums.PutawayStatus;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayAlreadyCompletedException;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayErrorCode;
import com.kb.cosmetic_wms.global.error.BusinessException;
import lombok.Getter;

@Getter
public class PutawayOrder {

    private Long id;
    private final Long inspectionId;
    private final Long lotId;
    private final Long productId;
    private final Long warehouseId;
    private final Long sourceSectionId;
    private final Long targetSectionId;
    private final int quantity;
    private PutawayStatus status;

    private PutawayOrder(Long id, Long inspectionId, Long lotId, Long productId,
                         Long warehouseId, Long sourceSectionId, Long targetSectionId,
                         int quantity, PutawayStatus status) {
        this.id = id;
        this.inspectionId = inspectionId;
        this.lotId = lotId;
        this.productId = productId;
        this.warehouseId = warehouseId;
        this.sourceSectionId = sourceSectionId;
        this.targetSectionId = targetSectionId;
        this.quantity = quantity;
        this.status = status;
    }

    public static PutawayOrder create(Long inspectionId, Long lotId, Long productId,
                                      Long warehouseId, Long sourceSectionId, Long targetSectionId,
                                      int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(PutawayErrorCode.INVALID_QUANTITY);
        }
        return new PutawayOrder(null, inspectionId, lotId, productId,
                warehouseId, sourceSectionId, targetSectionId, quantity, PutawayStatus.PENDING);
    }

    public static PutawayOrder reconstitute(Long id, Long inspectionId, Long lotId, Long productId,
                                            Long warehouseId, Long sourceSectionId, Long targetSectionId,
                                            int quantity, PutawayStatus status) {
        return new PutawayOrder(id, inspectionId, lotId, productId,
                warehouseId, sourceSectionId, targetSectionId, quantity, status);
    }

    public void complete() {
        if (!status.canTransitionTo(PutawayStatus.COMPLETED)) {
            throw new PutawayAlreadyCompletedException();
        }
        this.status = PutawayStatus.COMPLETED;
    }
}
