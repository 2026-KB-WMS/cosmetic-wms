package com.kb.cosmetic_wms.inbound.fixture;

import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundCreateRequest;
import com.kb.cosmetic_wms.inbound.adapter.in.web.InboundReceiveRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InboundRequestBuilder {

    private Long warehouseId = 1L;
    private Long partnerId = 1L;
    private LocalDateTime inboundDate = LocalDateTime.now().plusDays(1);
    private Long productId = 1L;
    private int orderedQuantity = 100;

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

    public InboundRequestBuilder quantity(int orderedQuantity) {
        this.orderedQuantity = orderedQuantity;
        return this;
    }

    public InboundCreateRequest buildCreateRequest() {
        InboundCreateRequest.LineRequest lineRequest = new InboundCreateRequest.LineRequest(productId, orderedQuantity,
                LocalDate.now().minusDays(10), LocalDate.now().plusYears(2));
        return new InboundCreateRequest(warehouseId, partnerId, inboundDate, List.of(lineRequest));
    }

    public InboundReceiveRequest buildReceiveRequest(Long lineId) {
        return new InboundReceiveRequest(List.of(new InboundReceiveRequest.LineItem(lineId, orderedQuantity)));
    }
}