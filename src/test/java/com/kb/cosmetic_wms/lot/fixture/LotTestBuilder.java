package com.kb.cosmetic_wms.lot.fixture;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.model.Lot;

import java.time.LocalDateTime;

public class LotTestBuilder {
    private Long inboundId = 1L;
    private String manufacturerLotNumber = "LOT0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private Long productId = 1L;

    public LotTestBuilder inboundId(Long inboundId) {
        this.inboundId = inboundId;
        return this;
    }

    public LotTestBuilder manufacturerLotNumber(String manufacturerLotNumber) {
        this.manufacturerLotNumber = manufacturerLotNumber;
        return this;
    }

    public LotTestBuilder manufacturingDate(LocalDateTime manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
        return this;
    }

    public LotTestBuilder expirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }

    public LotTestBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public Lot build() {
        return Lot.create(inboundId, manufacturerLotNumber, manufacturingDate, expirationDate, productId);
    }

    public Lot buildWithId(Long id) {
        return Lot.reconstitute(id, inboundId, manufacturerLotNumber,
                manufacturingDate, expirationDate, LotStatus.AVAILABLE, productId);
    }
}
