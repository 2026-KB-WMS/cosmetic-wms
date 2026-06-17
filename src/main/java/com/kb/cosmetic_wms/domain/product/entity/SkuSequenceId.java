package com.kb.cosmetic_wms.domain.product.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SkuSequenceId implements Serializable {

    private String brandName;
    private String categoryCode;
    private String typeCode;
    private int volume;
}
