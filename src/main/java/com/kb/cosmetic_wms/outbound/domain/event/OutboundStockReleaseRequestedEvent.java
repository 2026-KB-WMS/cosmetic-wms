package com.kb.cosmetic_wms.outbound.domain.event;

public record OutboundStockReleaseRequestedEvent(Long outboundId, Long ordersId) {}
