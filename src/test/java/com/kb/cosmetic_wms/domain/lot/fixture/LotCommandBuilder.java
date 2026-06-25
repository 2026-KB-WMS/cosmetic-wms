package com.kb.cosmetic_wms.lot.fixture;

import com.kb.cosmetic_wms.lot.application.port.in.RegisterLotCommand;

import java.time.LocalDateTime;

public class LotCommandBuilder {
    private String lotNumber = "SKN-240101-01-0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private Long productId = 1L;

    public LotCommandBuilder lotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
        return this;
    }

    public LotCommandBuilder productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public RegisterLotCommand build() {
        return new RegisterLotCommand(lotNumber, manufacturingDate, expirationDate, productId);
    }
}