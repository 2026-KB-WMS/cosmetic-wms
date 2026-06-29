package com.kb.cosmetic_wms.lot.fixture;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.model.Lot;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LotTestBuilder {
    private LocalDate inboundDate = LocalDate.of(2026, 1, 1);
    private Long inboundId = 1L;
    private String manufacturerLotNumber = "LOT0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private Long productId = 1L;

    public LotTestBuilder inboundDate(LocalDate inboundDate) {
        this.inboundDate = inboundDate;
        return this;
    }

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
        return Lot.create(inboundDate, inboundId, manufacturerLotNumber, manufacturingDate, expirationDate, productId);
    }

    public Lot buildWithId(Long id) {
        String lotNumber = String.format("%s-%d-%s",
                inboundDate.format(DateTimeFormatter.ofPattern("yyMMdd")), inboundId, manufacturerLotNumber);
        return Lot.reconstitute(id, lotNumber, manufacturingDate, expirationDate, LotStatus.AVAILABLE, productId);
    }
}
