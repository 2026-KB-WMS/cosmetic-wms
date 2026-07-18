package com.kb.ordering.product.application.port.in.dto;

public record RegisterCategoryCommand(
        String categoryCode,
        String categoryName
) {
}