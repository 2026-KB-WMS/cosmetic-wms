package com.kb.cosmetic_wms.domain.storage.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

    DUPLICATE_WAREHOUSE(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_WAREHOUSE",
            "이미 동일한 이름과 주소로 등록된 창고가 존재합니다."
    ),
    STORAGE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "STORAGE_NOT_FOUND",
            "지정한 창고 및 구역 마스터 데이터를 찾을 수 없습니다."
    ),
    EXCEED_WAREHOUSE_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "EXCEED_WAREHOUSE_CAPACITY",
            "하위 구역들의 총 최대 수용 용량 합이 창고 허용 용량(Capacity)을 초과할 수 없습니다."
    ),
    INVALID_STORAGE_STATE(
            HttpStatus.BAD_REQUEST,
            "INVALID_STORAGE_STATE",
            "창고 및 구역의 상태 조합 또는 설정이 올바르지 않습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
