package com.kb.cosmetic_wms.outbound.application.port.in;

public interface RecordPickingUseCase {

    /**
     * 출고 품목의 실제 피킹 수량을 누적이 아닌 절대값으로 설정한다. 같은 요청을 반복해도 결과가 같다.
     */
    OutboundDetailResult recordPicking(Long outboundId, Long outboundItemId, int pickedQuantity);
}
