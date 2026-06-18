package com.kb.cosmetic_wms.domain.lot.fixture;

import com.kb.cosmetic_wms.domain.lot.dto.LotCreateRequestDto;

import java.time.LocalDateTime;

public class LotDtoBuilder {
    private String lotNumber = "SKN-240101-01-0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private Long productId = 1L;

    public LotDtoBuilder lotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
        return this;
    }

    public LotDtoBuilder manufacturingDate(LocalDateTime manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
        return this;
    }

    public LotDtoBuilder expirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public LotDtoBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public LotCreateRequestDto build() {
        return new LotCreateRequestDto(lotNumber, manufacturingDate, expirationDate, productId);
    }
}
