package com.kb.ordering.product.application.port.in.dto;

public record RegisterProductTypeCommand(
        String typeCode,
        String typeName
) {
}
