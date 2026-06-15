package com.kb.cosmetic_wms.domain.store.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {
    STORE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "STORE_NOT_FOUND",
            "존재하지 않는 가맹점입니다."
    ),

    DUPLICATE_STORE(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_STORE",
            "이미 동일한 주소에 등록된 가맹점 점포가 존재합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
