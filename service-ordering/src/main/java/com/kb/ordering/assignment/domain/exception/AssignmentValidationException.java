package com.kb.ordering.assignment.domain.exception;

import com.kb.common.error.BusinessException;

public class AssignmentValidationException extends BusinessException {

    public AssignmentValidationException(AssignmentErrorCode errorCode) {
        super(errorCode);
    }
}
