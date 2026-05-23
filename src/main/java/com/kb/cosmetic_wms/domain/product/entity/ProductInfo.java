package com.kb.cosmetic_wms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skinType;
    private String functionType;

    @Embedded
    private Volume volume;

    private String ingredients;
    private String cautions;
    private String storageCondition;

    private ProductInfo(String skinType, String functionType, Volume volume,
                        String ingredients, String cautions, String storageCondition) {
        this.skinType = skinType;
        this.functionType = functionType;
        this.volume = volume;
        this.ingredients = ingredients;
        this.cautions = cautions;
        this.storageCondition = storageCondition;
    }

    public static ProductInfo create(String skinType, String functionType, Volume volume,
                                     String ingredients, String cautions, String storageCondition) {
        return new ProductInfo(skinType, functionType, volume, ingredients, cautions, storageCondition);
    }
}
