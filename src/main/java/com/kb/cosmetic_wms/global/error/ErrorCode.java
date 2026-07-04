package com.kb.cosmetic_wms.global.error;

public interface ErrorCode {
    ErrorType getType();

    String getCode();

    String getMessage();
}
