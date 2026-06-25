package com.kb.cosmetic_wms.inventory.domain.exception;

import com.kb.cosmetic_wms.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {

    INVENTORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "INVENTORY_NOT_FOUND",
            "존재하지 않는 재고입니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}