package com.kb.cosmetic_wms.product.domain.model;

import com.kb.cosmetic_wms.product.domain.constants.ProductInfoConstants;
import com.kb.cosmetic_wms.product.domain.valueobject.Volume;
import lombok.Getter;

@Getter
public class ProductInfo {

    private final String skinType;
    private final String functionType;
    private final Volume volume;
    private final String ingredients;
    private final String cautions;
    private final String storageCondition;

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
        String normalizedSkinType = normalizeBlank(skinType);
        String normalizedFunctionType = normalizeBlank(functionType);
        String normalizedStorageCondition = normalizeBlank(storageCondition);
        String normalizedIngredients = normalizeBlank(ingredients);
        String normalizedCautions = normalizeBlank(cautions);

        validateSkinTypeLength(normalizedSkinType);
        validateFunctionTypeLength(normalizedFunctionType);
        validateStorageConditionLength(normalizedStorageCondition);

        return new ProductInfo(
                normalizedSkinType, normalizedFunctionType, volume,
                normalizedIngredients, normalizedCautions, normalizedStorageCondition
        );
    }

    public static ProductInfo reconstitute(String skinType, String functionType, Volume volume,
                                           String ingredients, String cautions, String storageCondition) {
        return new ProductInfo(skinType, functionType, volume, ingredients, cautions, storageCondition);
    }

    private static String normalizeBlank(String input) {
        if (input == null || input.isBlank()) return "";
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