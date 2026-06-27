package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.exception.InboundItemProductRequiredException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundInvalidQuantityException;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class InboundLine {

    private Long id;
    private Long productId;
    private InboundQuantity orderedQuantity;
    private int receivedQuantity;
    private LotDateRange lotDateRange;

    private InboundLine() {
    }

    public static InboundLine create(Long productId, int orderedQuantity,
                                     LocalDate manufactureDate, LocalDate expirationDate) {
        validateProductId(productId);
        InboundLine line = new InboundLine();
        line.productId = productId;
        line.orderedQuantity = new InboundQuantity(orderedQuantity);
        line.lotDateRange = new LotDateRange(manufactureDate, expirationDate);
        line.receivedQuantity = 0;
        return line;
    }

    public static InboundLine reconstitute(Long id, Long productId, int orderedQuantity, int receivedQuantity,
                                           LocalDate manufactureDate, LocalDate expirationDate) {
        InboundLine line = new InboundLine();
        line.id = id;
        line.productId = productId;
        line.orderedQuantity = new InboundQuantity(orderedQuantity);
        line.receivedQuantity = receivedQuantity;
        line.lotDateRange = new LotDateRange(manufactureDate, expirationDate);
        return line;
    }

    public void receive(int receivedQuantity) {
        if (receivedQuantity < 0) {
            throw new InboundInvalidQuantityException();
        }
        this.receivedQuantity = receivedQuantity;
    }

    public int getOrderedQuantity() {
        return orderedQuantity.value();
    }

    public LocalDate getManufactureDate() {
        return lotDateRange.manufactureDate();
    }

    public LocalDate getExpirationDate() {
        return lotDateRange.expirationDate();
    }

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new InboundItemProductRequiredException();
        }
    }
}