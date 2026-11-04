package com.kb.cosmetic_wms.inventory.application.port.in;

public interface ManageLotBlockUseCase {

    /**
     * 로트를 출고 대상에서 제외한다. 이미 차단된 로트이면 사유만 갱신한다.
     */
    void block(Long lotId, String status);

    void release(Long lotId);
}
