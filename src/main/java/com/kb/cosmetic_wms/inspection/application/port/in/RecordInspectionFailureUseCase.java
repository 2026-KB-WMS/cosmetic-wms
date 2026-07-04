package com.kb.cosmetic_wms.inspection.application.port.in;

/**
 * 검사 전표 생성이 재시도 끝에 실패했을 때 실패 이력을 기록한다.
 */
public interface RecordInspectionFailureUseCase {

    void record(RecordInspectionFailureCommand command);
}
