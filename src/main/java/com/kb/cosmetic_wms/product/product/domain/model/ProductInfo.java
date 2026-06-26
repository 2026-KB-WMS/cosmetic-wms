package com.kb.cosmetic_wms.product.product.domain.model;

import com.kb.cosmetic_wms.product.product.domain.exception.InvalidProductInfoException;
import com.kb.cosmetic_wms.product.product.domain.valueobject.Volume;
import lombok.Getter;

@Getter
public class ProductInfo {

    public static final int SKIN_TYPE_MAX_LENGTH = 50;
    public static final int FUNCTION_TYPE_MAX_LENGTH = 100;
    public static final int STORAGE_CONDITION_MAX_LENGTH = 255;

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

        if (normalizedSkinType.length() > SKIN_TYPE_MAX_LENGTH) {
            throw new InvalidProductInfoException(
                    "피부 타입 정보는 " + SKIN_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
        if (normalizedFunctionType.length() > FUNCTION_TYPE_MAX_LENGTH) {
            throw new InvalidProductInfoException(
                    "기능성 타입 정보는 " + FUNCTION_TYPE_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }
        if (normalizedStorageCondition.length() > STORAGE_CONDITION_MAX_LENGTH) {
            throw new InvalidProductInfoException(
                    "보관 조건 정보는 " + STORAGE_CONDITION_MAX_LENGTH + "자를 초과할 수 없습니다.");
        }

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
}