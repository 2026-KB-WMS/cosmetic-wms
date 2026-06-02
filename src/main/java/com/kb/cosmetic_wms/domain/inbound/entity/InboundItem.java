package com.kb.cosmetic_wms.domain.inbound.entity;

import com.kb.cosmetic_wms.domain.inbound.constants.InboundConstants;
import com.kb.cosmetic_wms.domain.inbound.enums.InspectionStatus;
import com.kb.cosmetic_wms.domain.inventory.entity.Lot;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.storage.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class InboundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    private int quantity;

    private LocalDateTime manufactureDate;

    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    private InspectionStatus inspectionStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Inbound inbound;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    private Lot lot;

    @ManyToOne(fetch = FetchType.LAZY)
    private Section section;

    private InboundItem(Inbound inbound, Product product, int quantity,
                        LocalDateTime manufactureDate, LocalDateTime expirationDate
    ) {
        this.inbound = inbound;
        this.product = product;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
        this.inspectionStatus = InspectionStatus.WAITING;
    }

    public static InboundItem create(Inbound inbound, Product product, int quantity,
                                     LocalDateTime manufactureDate, LocalDateTime expirationDate
    ) {
        validateQuantity(quantity);
        return new InboundItem(inbound, product, quantity, manufactureDate, expirationDate);
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(InboundConstants.INVALID_INBOUND_QUANTITY_MESSAGE);
        }
    }
}
