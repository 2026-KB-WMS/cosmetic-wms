package com.kb.cosmetic_wms.product.producttype.application.port.in;

public record RegisterProductTypeCommand(
        String typeCode,
        String typeName
) {
}
