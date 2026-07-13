package com.kb.common.error;

public interface ErrorCode {
    ErrorType getType();

    String getCode();

    String getMessage();
}