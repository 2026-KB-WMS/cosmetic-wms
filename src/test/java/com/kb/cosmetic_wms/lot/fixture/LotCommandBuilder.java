package com.kb.cosmetic_wms.lot.fixture;

import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;

import java.time.LocalDateTime;

public class LotCommandBuilder {
    private Long inboundId = 1L;
    private String manufacturerLotNumber = "LOT0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private Long productId = 1L;

    public LotCommandBuilder inboundId(Long inboundId) {
        this.inboundId = inboundId;
        return this;
    }

    public LotCommandBuilder manufacturerLotNumber(String manufacturerLotNumber) {
        this.manufacturerLotNumber = manufacturerLotNumber;
        return this;
    }

    public LotCommandBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public RegisterLotCommand build() {
        return new RegisterLotCommand(inboundId, manufacturerLotNumber,
                manufacturingDate, expirationDate, productId);
    }
}
