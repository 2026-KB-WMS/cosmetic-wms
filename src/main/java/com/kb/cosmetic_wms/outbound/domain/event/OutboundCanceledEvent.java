package com.kb.cosmetic_wms.outbound.domain.event;

public record OutboundCanceledEvent(Long outboundId, Long ordersId) {}
