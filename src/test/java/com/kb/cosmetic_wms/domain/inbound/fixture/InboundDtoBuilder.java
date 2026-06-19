package com.kb.cosmetic_wms.domain.inbound.fixture;

import com.kb.cosmetic_wms.domain.inbound.dto.InboundCreateRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundItemAddRequestDto;
import com.kb.cosmetic_wms.domain.inbound.dto.InboundPutawayRequestDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InboundDtoBuilder {

    private Long warehouseId = 1L;
    private Long partnerId = 1L;
    private LocalDateTime inboundDate = LocalDateTime.now().plusDays(1);
    private Long productId = 1L;
    private int quantity = 100;
    private Long lotId = 100L;
    private Long sectionId = 200L;

    public InboundDtoBuilder warehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public InboundDtoBuilder partnerId(Long partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public InboundDtoBuilder inboundDate(LocalDateTime inboundDate) {
        this.inboundDate = inboundDate;
        return this;
    }

    public InboundDtoBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public InboundDtoBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public InboundCreateRequestDto buildCreateRequest() {
        return new InboundCreateRequestDto(warehouseId, partnerId, inboundDate);
    }

    public InboundItemAddRequestDto buildAddItemRequest() {
        return new InboundItemAddRequestDto(
                productId, quantity,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusYears(2)
        );
    }

    public InboundPutawayRequestDto buildPutawayRequest() {
        return new InboundPutawayRequestDto(lotId, sectionId);
    }
}
