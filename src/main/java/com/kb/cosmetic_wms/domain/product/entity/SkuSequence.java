package com.kb.cosmetic_wms.domain.product.entity;

import com.kb.cosmetic_wms.domain.product.constants.ProductConstants;
import com.kb.cosmetic_wms.domain.product.exception.SkuSequenceOverflowException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sku_sequence")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SkuSequence {

    @EmbeddedId
    private SkuSequenceId id;

    @Column(name = "current_seq", nullable = false)
    private int currentSeq;

    public static SkuSequence init(String brandName, String categoryCode, String typeCode, int volume) {
        SkuSequence seq = new SkuSequence();
        seq.id = new SkuSequenceId(brandName, categoryCode, typeCode, volume);
        seq.currentSeq = 0;
        return seq;
    }

    public int incrementAndGet() {
        if (this.currentSeq >= ProductConstants.SEQUENCE_MAX_BOUND) {
            throw new SkuSequenceOverflowException();
        }
        return ++this.currentSeq;
    }
}
