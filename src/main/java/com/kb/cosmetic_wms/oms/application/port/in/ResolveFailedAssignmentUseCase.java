package com.kb.cosmetic_wms.oms.application.port.in;

public interface ResolveFailedAssignmentUseCase {

    /**
     * 실패한 창고 배정을 다시 시도한다. 시도가 실패해도 예외를 던지지 않고 최근 오류를 기록한 결과를 돌려준다.
     */
    FailedAssignmentResult retry(Long failedEventId);

    FailedAssignmentResult discard(Long failedEventId);
}
