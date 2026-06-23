package com.kb.cosmetic_wms.global.event;

public record OutboundStockReleaseRequestedEvent(Long outboundId, Long ordersId) {}