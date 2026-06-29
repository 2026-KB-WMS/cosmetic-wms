package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotStatusTransitionException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductIdRequiredException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class Lot {

    private final Long id;
    private final LotNumber lotNumber;
    private final LotPeriod period;
    private LotStatus status;
    private final Long productId;

    private Lot(Long id, LotNumber lotNumber, LotPeriod period, LotStatus status, Long productId) {
        this.id = id;
        this.lotNumber = lotNumber;
        this.period = period;
        this.status = status;
        this.productId = productId;
    }

    public static Lot create(LocalDate inboundDate, Long inboundId, String manufacturerLotNumber,
                             LocalDateTime manufacturingDate, LocalDateTime expirationDate, Long productId) {
        if (productId == null) {
            throw new LotProductIdRequiredException();
        }
        return new Lot(null, LotNumber.of(inboundDate, inboundId, manufacturerLotNumber),
                new LotPeriod(manufacturingDate, expirationDate), LotStatus.AVAILABLE, productId);
    }

    public static Lot reconstitute(Long id, String lotNumber, LocalDateTime manufacturingDate,
                                   LocalDateTime expirationDate, LotStatus status, Long productId) {
        return new Lot(id, new LotNumber(lotNumber), new LotPeriod(manufacturingDate, expirationDate),
                status, productId);
    }

    public void changeStatus(LotStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidLotStatusTransitionException(status, newStatus);
        }
        this.status = newStatus;
    }
}