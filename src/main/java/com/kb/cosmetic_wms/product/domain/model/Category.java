package com.kb.cosmetic_wms.product.domain.model;

import com.kb.cosmetic_wms.product.domain.constants.CategoryConstants;
import lombok.Getter;

@Getter
public class Category {

    private final Long categoryId;
    private final String categoryCode;
    private final String categoryName;

    private Category(Long categoryId, String categoryCode, String categoryName) {
        this.categoryId = categoryId;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public static Category create(String categoryCode, String categoryName) {
        validateCategoryCode(categoryCode);
        validateCategoryName(categoryName);
        return new Category(null, categoryCode, categoryName);
    }

    public static Category reconstitute(Long categoryId, String categoryCode, String categoryName) {
        return new Category(categoryId, categoryCode, categoryName);
    }

    private static void validateCategoryCode(String categoryCode) {
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new IllegalArgumentException(CategoryConstants.CATEGORY_CODE_REQUIRED_MESSAGE);
        }
        if (categoryCode.length() != CategoryConstants.CATEGORY_CODE_LENGTH) {
            throw new IllegalArgumentException(CategoryConstants.INVALID_CATEGORY_CODE_LENGTH_MESSAGE);
        }
        if (!CategoryConstants.CATEGORY_CODE_PATTERN.matcher(categoryCode).matches()) {
            throw new IllegalArgumentException(CategoryConstants.INVALID_CATEGORY_CODE_FORMAT_MESSAGE);
        }
    }

    private static void validateCategoryName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new IllegalArgumentException(CategoryConstants.CATEGORY_NAME_REQUIRED_MESSAGE);
        }
    }
}