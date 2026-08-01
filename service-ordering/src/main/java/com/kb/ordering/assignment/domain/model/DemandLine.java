package com.kb.ordering.assignment.domain.model;

import com.kb.ordering.assignment.domain.exception.AssignmentErrorCode;
import com.kb.ordering.assignment.domain.exception.AssignmentValidationException;

public record DemandLine(Long productId, int quantity) {

    public DemandLine {
        if (productId == null || quantity <= 0) {
            throw new AssignmentValidationException(AssignmentErrorCode.INVALID_DEMAND);
        }
    }
}
