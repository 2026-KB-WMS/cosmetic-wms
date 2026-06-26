package com.kb.cosmetic_wms.inbound.fixture;

import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundCreateRequest;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundItemAddRequest;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundPutawayRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InboundRequestBuilder {

    private Long warehouseId = 1L;
    private Long partnerId = 1L;
    private LocalDateTime inboundDate = LocalDateTime.now().plusDays(1);
    private Long productId = 1L;
    private int quantity = 100;
    private Long lotId = 100L;
    private Long sectionId = 200L;

    public InboundRequestBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public InboundRequestBuilder partnerId(Long partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public InboundRequestBuilder inboundDate(LocalDateTime inboundDate) {
        this.inboundDate = inboundDate;
        return this;
    }

    public InboundRequestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public InboundRequestBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InboundCreateRequest buildCreateRequest() {
        return new InboundCreateRequest(warehouseId, partnerId, inboundDate);
    }

    public InboundItemAddRequest buildAddItemRequest() {
        return new InboundItemAddRequest(productId, quantity,
                LocalDate.now().minusDays(10), LocalDate.now().plusYears(2));
    }

    public InboundPutawayRequest buildPutawayRequest() {
        return new InboundPutawayRequest(lotId, sectionId);
    }
}
