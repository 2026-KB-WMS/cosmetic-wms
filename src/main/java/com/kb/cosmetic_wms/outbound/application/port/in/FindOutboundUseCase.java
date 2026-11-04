package com.kb.cosmetic_wms.outbound.application.port.in;

import java.util.List;

public interface FindOutboundUseCase {

    /**
     * 해당 로트의 재고가 할당되어 있고 아직 출하·취소되지 않은(ALLOCATED, PROCESSING) 출고 전표.
     * 리콜·보류된 로트의 영향을 받는 출고를 사람이 확인하기 위한 조회이며, 자동으로 취소하지 않는다.
     */
    List<OutboundResult> findInProgressByLotId(Long lotId);

    OutboundDetailResult findById(Long outboundId);
}
