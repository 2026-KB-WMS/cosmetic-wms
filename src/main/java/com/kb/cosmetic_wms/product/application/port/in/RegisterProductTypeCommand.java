package com.kb.cosmetic_wms.product.application.port.in;

public record RegisterProductTypeCommand(
        String typeCode,
        String typeName
) {
}