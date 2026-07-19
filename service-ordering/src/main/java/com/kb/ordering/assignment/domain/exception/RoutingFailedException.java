package com.kb.ordering.assignment.domain.exception;

import com.kb.common.error.BusinessException;

public class RoutingFailedException extends BusinessException {

    public RoutingFailedException() {
        super(AssignmentErrorCode.ROUTING_API_ERROR);
    }
}
