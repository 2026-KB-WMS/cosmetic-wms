package com.kb.cosmetic_wms.inventory.application.port.in;

import java.util.List;

public interface FindLotAllocationUseCase {

    /**
     * 해당 로트의 재고가 현재 할당되어 있는 출고의 ID 목록(오름차순).
     * 같은 로트·구역의 할당은 출고가 달라도 한 재고 행으로 병합되므로, 출하·취소가 끝난 출고가 섞여 있을 수 있다.
     * 호출하는 쪽이 자기 도메인의 출고 상태로 한 번 더 걸러야 한다.
     */
    List<Long> findAllocatedOutboundIds(Long lotId);
}
