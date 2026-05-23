package com.kb.cosmetic_wms.domain.product.productInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private int volume;
    private String unit;
    private String ingredients;
    private String cautions;
    private String storageCondition;

    private ProductInfo(String skinType, String functionType, int volume, String unit,
                        String ingredients, String cautions, String storageCondition) {
        this.skinType = skinType;
        this.functionType = functionType;
        this.volume = volume;
        this.unit = unit;
        this.ingredients = ingredients;
        this.cautions = cautions;
        this.storageCondition = storageCondition;
    }

    public static ProductInfo create(String skinType, String functionType, int volume, String unit,
                                     String ingredients, String cautions, String storageCondition) {
        validateVolume(volume);

        return new ProductInfo(skinType, functionType, volume, unit, ingredients, cautions, storageCondition);
    }

    private static void validateVolume(int volume) {
        if (volume <= ProductInfoConstants.MIN_VOLUME) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_VOLUME_MIN_MESSAGE);
        }
        if (volume > ProductInfoConstants.MAX_VOLUME) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_VOLUME_MAX_MESSAGE);
        }
    }
}
