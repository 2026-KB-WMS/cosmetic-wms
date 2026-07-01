package com.kb.cosmetic_wms.outbound.domain.event;

public record OutboundShippedEvent(Long outboundId, Long ordersId) {}