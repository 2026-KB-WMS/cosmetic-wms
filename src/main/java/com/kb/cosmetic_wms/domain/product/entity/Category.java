package com.kb.cosmetic_wms.domain.product.entity;

import com.kb.cosmetic_wms.domain.product.CategoryConstants;
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
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryCode;
    private String categoryName;

    private Category(String categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public static Category create(String categoryCode, String categoryName) {
        validateCategoryCode(categoryCode);
        validateCategoryName(categoryName);

        return new Category(categoryCode, categoryName);
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
