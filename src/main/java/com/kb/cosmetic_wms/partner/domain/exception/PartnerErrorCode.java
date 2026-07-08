package com.kb.cosmetic_wms.partner.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import com.kb.cosmetic_wms.global.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartnerErrorCode implements ErrorCode {
    PARTNER_NOT_FOUND(
            ErrorType.NOT_FOUND,
            "PARTNER_NOT_FOUND",
            "존재하지 않는 협력사입니다."
    ),

    DUPLICATE_PARTNER(
            ErrorType.INVALID_REQUEST,
            "DUPLICATE_PARTNER",
            "이미 등록된 동일한 사업자 번호의 협력사가 존재합니다."
    ),

    PARTNER_VALIDATION_FAILED(
            ErrorType.INVALID_REQUEST,
            "PARTNER_VALIDATION_FAILED",
            "협력사 정보 유효성 검증에 실패했습니다."
    );

    private final ErrorType type;
    private final String code;
    private final String message;
}