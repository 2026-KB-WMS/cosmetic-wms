package com.kb.cosmetic_wms.lot.domain.model;

import com.kb.cosmetic_wms.lot.domain.enums.LotStatus;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotNumberFormatException;
import com.kb.cosmetic_wms.lot.domain.exception.InvalidLotStatusTransitionException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductIdRequiredException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Getter
public class Lot {

    private static final Pattern MANUFACTURER_LOT_PATTERN = Pattern.compile("^[A-Z0-9][A-Z0-9\\-]{0,19}$");

    private final Long id;
    private final Long inboundId;
    private final String manufacturerLotNumber;
    private final LotPeriod period;
    private LotStatus status;
    private final Long productId;

    private Lot(Long id, Long inboundId, String manufacturerLotNumber,
                LotPeriod period, LotStatus status, Long productId) {
        this.id = id;
        this.inboundId = inboundId;
        this.manufacturerLotNumber = manufacturerLotNumber;
        this.period = period;
        this.status = status;
        this.productId = productId;
    }

    public static Lot create(Long inboundId, String manufacturerLotNumber,
                             LocalDateTime manufacturingDate, LocalDateTime expirationDate, Long productId) {
        if (productId == null) {
            throw new LotProductIdRequiredException();
        }
        if (manufacturerLotNumber == null || !MANUFACTURER_LOT_PATTERN.matcher(manufacturerLotNumber).matches()) {
            throw new InvalidLotNumberFormatException();
        }
        return new Lot(null, inboundId, manufacturerLotNumber,
                new LotPeriod(manufacturingDate, expirationDate), LotStatus.AVAILABLE, productId);
    }

    public static Lot reconstitute(Long id, Long inboundId, String manufacturerLotNumber,
                                   LocalDateTime manufacturingDate, LocalDateTime expirationDate,
                                   LotStatus status, Long productId) {
        return new Lot(id, inboundId, manufacturerLotNumber,
                new LotPeriod(manufacturingDate, expirationDate), status, productId);
    }

    public void changeStatus(LotStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidLotStatusTransitionException(status, newStatus);
        }
        this.status = newStatus;
    }
}
