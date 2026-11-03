package com.kb.cosmetic_wms.inspection.application.port.in;

public interface ResolveFailedInspectionUseCase {

    /**
     * 실패한 검사 전표 생성을 다시 시도한다. 시도가 실패해도 예외를 던지지 않고 최근 오류를 기록한 결과를 돌려준다.
     */
    FailedInspectionResult retry(Long failedEventId);

    FailedInspectionResult discard(Long failedEventId);
}
