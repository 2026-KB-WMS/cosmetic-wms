package com.kb.cosmetic_wms.domain.product.entity;

import com.kb.cosmetic_wms.domain.product.constants.ProductInfoConstants;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductInfo {
    
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

        String normalizedSkinType = convertToEmptyIfBlank(skinType);
        String normalizedFunctionType = convertToEmptyIfBlank(functionType);
        String normalizedStorageCondition = convertToEmptyIfBlank(storageCondition);
        String normalizedIngredients = convertToEmptyIfBlank(ingredients);
        String normalizedCautions = convertToEmptyIfBlank(cautions);

        validateSkinTypeLength(normalizedSkinType);
        validateFunctionTypeLength(normalizedFunctionType);
        validateStorageConditionLength(normalizedStorageCondition);

        return new ProductInfo(
                normalizedSkinType,
                normalizedFunctionType,
                volume,
                normalizedIngredients,
                normalizedCautions,
                normalizedStorageCondition
        );
    }

    private static String convertToEmptyIfBlank(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return input.trim();
    }

    private static void validateSkinTypeLength(String skinType) {
        if (skinType.length() > ProductInfoConstants.SKIN_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_SKIN_TYPE_LENGTH_MESSAGE);
        }
    }

    private static void validateFunctionTypeLength(String functionType) {
        if (functionType.length() > ProductInfoConstants.FUNCTION_TYPE_MAX_LENGTH) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_FUNCTION_TYPE_LENGTH_MESSAGE);
        }
    }

    private static void validateStorageConditionLength(String storageCondition) {
        if (storageCondition.length() > ProductInfoConstants.STORAGE_CONDITION_MAX_LENGTH) {
            throw new IllegalArgumentException(ProductInfoConstants.INVALID_STORAGE_CONDITION_LENGTH_MESSAGE);
        }
    }
}
