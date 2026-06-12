package com.kb.cosmetic_wms.global.error;

import com.kb.cosmetic_wms.domain.member.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.domain.member.exception.LoginFailedException;
import com.kb.cosmetic_wms.domain.member.exception.MemberNotFoundException;
import com.kb.cosmetic_wms.global.error.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleMemberNotFoundException(MemberNotFoundException e) {
        log.warn("MemberNotFoundException 발생: {}", e.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.of("MEMBER_NOT_FOUND", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateMemberException(DuplicateMemberException e) {
        log.warn("DuplicateMemberException 발생: {}", e.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.of("DUPLICATE_LOGIN_ID", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleLoginFailedException(LoginFailedException e) {
        log.warn("LoginFailedException 발생: {}", e.getMessage());

        ErrorResponseDto errorResponse = ErrorResponseDto.of("LOGIN_FAILED", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}
