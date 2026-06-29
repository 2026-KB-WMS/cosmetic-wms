package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.exception.InboundInvalidQuantityException;
import com.kb.cosmetic_wms.inbound.domain.exception.InboundItemProductRequiredException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InboundLine {

    private Long id;
    private Long productId;
    private InboundQuantity orderedQuantity;
    private int receivedQuantity;
    private String manufacturerLotNumber;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expirationDate;

    private InboundLine() {
    }

    public static InboundLine create(Long productId, int orderedQuantity) {
        validateProductId(productId);
        InboundLine line = new InboundLine();
        line.productId = productId;
        line.orderedQuantity = new InboundQuantity(orderedQuantity);
        line.receivedQuantity = 0;
        return line;
    }

    public static InboundLine reconstitute(Long id, Long productId, int orderedQuantity, int receivedQuantity,
                                           String manufacturerLotNumber, LocalDateTime manufacturingDate,
                                           LocalDateTime expirationDate) {
        InboundLine line = new InboundLine();
        line.id = id;
        line.productId = productId;
        line.orderedQuantity = new InboundQuantity(orderedQuantity);
        line.receivedQuantity = receivedQuantity;
        line.manufacturerLotNumber = manufacturerLotNumber;
        line.manufacturingDate = manufacturingDate;
        line.expirationDate = expirationDate;
        return line;
    }

    public void receive(int receivedQuantity, String manufacturerLotNumber,
                        LocalDateTime manufacturingDate, LocalDateTime expirationDate) {
        if (receivedQuantity < 0) {
            throw new InboundInvalidQuantityException();
        }
        this.receivedQuantity = receivedQuantity;
        this.manufacturerLotNumber = manufacturerLotNumber;
        this.manufacturingDate = manufacturingDate;
        this.expirationDate = expirationDate;
    }

    public int getOrderedQuantity() {
        return orderedQuantity.value();
    }

    private static void validateProductId(Long productId) {
        if (productId == null) {
            throw new InboundItemProductRequiredException();
        }
    }
}
