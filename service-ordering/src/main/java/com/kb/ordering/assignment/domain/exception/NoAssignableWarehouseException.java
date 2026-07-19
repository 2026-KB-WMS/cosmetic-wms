package com.kb.ordering.assignment.domain.exception;

import com.kb.common.error.BusinessException;

public class NoAssignableWarehouseException extends BusinessException {

    public NoAssignableWarehouseException() {
        super(AssignmentErrorCode.NO_ASSIGNABLE_WAREHOUSE);
    }
}
