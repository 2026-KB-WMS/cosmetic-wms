package com.kb.cosmetic_wms.outbound.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OutboundType {
    ORDER("가맹점 발주 출고");

    private final String description;
}