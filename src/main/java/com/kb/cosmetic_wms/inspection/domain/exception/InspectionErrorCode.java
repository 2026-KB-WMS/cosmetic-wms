package com.kb.cosmetic_wms.inspection.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InspectionErrorCode implements ErrorCode {

    INSPECTION_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INSPECTION_NOT_FOUND",
            "존재하지 않는 품질 검사 전표입니다."
    ),
    INSPECTION_SOURCE_TYPE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_SOURCE_TYPE_REQUIRED",
            "품질 검사 요청 출처(SourceType)는 필수입니다."
    ),
    INSPECTION_SOURCE_ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_SOURCE_ID_REQUIRED",
            "출처 대상 ID(SourceId)는 필수입니다."
    ),
    INSPECTION_QUANTITY_INVALID(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_QUANTITY_INVALID",
            "품질 검사 대상 총 수량은 0 이하일 수 없습니다."
    ),
    INSPECTION_INSPECTOR_ID_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_INSPECTOR_ID_REQUIRED",
            "품질 검사자 식별자(ID)는 필수입니다."
    ),
    INSPECTION_START_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "INSPECTION_START_NOT_ALLOWED",
            "대기(WAITING) 상태의 전표만 검사를 시작할 수 있습니다."
    ),
    INSPECTION_NEGATIVE_QUANTITY(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_NEGATIVE_QUANTITY",
            "합격 수량 또는 반려 수량은 음수일 수 없습니다."
    ),
    INSPECTION_QUANTITY_MISMATCH(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INSPECTION_QUANTITY_MISMATCH",
            "합격 수량과 반려 수량의 합이 총 검사 수량과 일치해야 합니다."
    ),
    INSPECTION_DEFECT_REASON_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "INSPECTION_DEFECT_REASON_REQUIRED",
            "품질 검사 결과가 불합격일 경우 부적합 사유는 필수입니다."
    ),
    INSPECTION_COMPLETE_NOT_ALLOWED(
            HttpStatus.CONFLICT,
            "INSPECTION_COMPLETE_NOT_ALLOWED",
            "진행 중(IN_PROGRESS) 상태인 전표만 판정을 완료할 수 있습니다."
    ),
    INSPECTION_LOT_NOT_FOUND(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "INSPECTION_LOT_NOT_FOUND",
            "검사 대상 로트를 찾을 수 없습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}