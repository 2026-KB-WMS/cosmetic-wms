package com.kb.cosmetic_wms.storage.domain.exception;

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
    ),
    INVALID_WAREHOUSE_NAME(
            HttpStatus.BAD_REQUEST,
            "INVALID_WAREHOUSE_NAME",
            "창고 이름은 필수 입력 항목입니다."
    ),
    INVALID_ADDRESS(
            HttpStatus.BAD_REQUEST,
            "INVALID_ADDRESS",
            "창고 주소는 필수 입력 항목입니다."
    ),
    INVALID_TARGET_TEMP(
            HttpStatus.BAD_REQUEST,
            "INVALID_TARGET_TEMP",
            "창고 적정 온도 포맷이 올바르지 않습니다. (예: 10~25도)"
    ),
    INVALID_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "INVALID_CAPACITY",
            "창고 수용 한도는 0보다 커야 합니다."
    ),
    DUPLICATE_SECTION_CODE(
            HttpStatus.BAD_REQUEST,
            "DUPLICATE_SECTION_CODE",
            "해당 창고에 이미 동일한 섹션 코드가 존재합니다."
    ),
    INVALID_SECTION_CODE(
            HttpStatus.BAD_REQUEST,
            "INVALID_SECTION_CODE",
            "섹션 코드 포맷이 올바르지 않습니다. (예: WH01-HIGH-R-01)"
    ),
    INVALID_SECTION_NAME(
            HttpStatus.BAD_REQUEST,
            "INVALID_SECTION_NAME",
            "섹션 이름은 필수 입력 항목입니다."
    ),
    INVALID_SECTION_MAX_CAPACITY(
            HttpStatus.BAD_REQUEST,
            "INVALID_SECTION_MAX_CAPACITY",
            "섹션의 최대 수용 가능 수량은 0보다 커야 합니다."
    ),
    SECTION_CAPACITY_OVERFLOW(
            HttpStatus.BAD_REQUEST,
            "SECTION_CAPACITY_OVERFLOW",
            "섹션의 최대 수용 가능 수량을 초과할 수 없습니다."
    ),
    SECTION_CAPACITY_UNDERFLOW(
            HttpStatus.BAD_REQUEST,
            "SECTION_CAPACITY_UNDERFLOW",
            "섹션의 현재 수량이 0보다 작아질 수 없습니다."
    ),
    INVALID_SECTION_SEQUENCE(
            HttpStatus.BAD_REQUEST,
            "INVALID_SECTION_SEQUENCE",
            "섹션 발행 순번은 1 이상이어야 합니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
