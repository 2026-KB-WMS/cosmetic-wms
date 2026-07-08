package com.kb.cosmetic_wms.outbound.application.port.in;

/**
 * 창고 배정 확정에 따라 출고 전표 생성 → FEFO 재고 할당 → 발주 준비 전환까지 처리한다.
 */
public interface CreateOutboundFromAssignmentUseCase {

    OutboundResult createFromAssignment(CreateOutboundFromAssignmentCommand command);
}
