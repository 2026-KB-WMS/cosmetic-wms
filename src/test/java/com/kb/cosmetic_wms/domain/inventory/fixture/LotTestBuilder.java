package com.kb.cosmetic_wms.domain.inventory.fixture;

import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.inventory.enums.LotStatus;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.fixture.ProductTestBuilder;

import java.time.LocalDateTime;

public class LotTestBuilder {
    private String lotNumber = "SKN-240101-01-0001";
    private LocalDateTime manufacturingDate = LocalDateTime.of(2026, 1, 1, 0, 0);
    private LocalDateTime expirationDate = LocalDateTime.of(2027, 1, 1, 0, 0);
    private LotStatus status = LotStatus.AVAILABLE;
    private Product product = new ProductTestBuilder().build();

    public LotTestBuilder lotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
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

    public LotTestBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public Lot build() {
        return Lot.create(
                lotNumber,
                manufacturingDate,
                expirationDate,
                product
        );
    }
}
