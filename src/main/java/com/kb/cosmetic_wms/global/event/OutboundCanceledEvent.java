package com.kb.cosmetic_wms.global.event;

public record OutboundCanceledEvent(Long outboundId, Long ordersId) {}