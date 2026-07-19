package com.kb.ordering.assignment.domain.model;

import com.kb.ordering.assignment.domain.exception.AssignmentErrorCode;
import com.kb.ordering.assignment.domain.exception.AssignmentValidationException;

import java.time.LocalDate;

public record ProductStock(int availableQuantity, LocalDate earliestExpiryDate) {

    public ProductStock {
        if (availableQuantity < 0) {
            throw new AssignmentValidationException(AssignmentErrorCode.INVALID_PRODUCT_STOCK);
        }
        if (availableQuantity > 0 && earliestExpiryDate == null) {
            throw new AssignmentValidationException(AssignmentErrorCode.INVALID_PRODUCT_STOCK);
        }
    }

    public static ProductStock empty() {
        return new ProductStock(0, null);
    }
}
