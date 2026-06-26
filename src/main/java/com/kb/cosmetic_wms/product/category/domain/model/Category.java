package com.kb.cosmetic_wms.product.category.domain.model;

import com.kb.cosmetic_wms.product.category.domain.exception.InvalidCategoryNameException;
import com.kb.cosmetic_wms.product.category.domain.valueobject.CategoryCode;
import lombok.Getter;

@Getter
public class Category {

    private final Long categoryId;
    @Getter(lombok.AccessLevel.NONE)
    private final CategoryCode categoryCode;
    private final String categoryName;

    private Category(Long categoryId, CategoryCode categoryCode, String categoryName) {
        this.categoryId = categoryId;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public static Category create(String categoryCode, String categoryName) {
        CategoryCode code = new CategoryCode(categoryCode);
        if (categoryName == null || categoryName.isBlank()) {
            throw new InvalidCategoryNameException();
        }
        return new Category(null, code, categoryName);
    }

    public static Category reconstitute(Long categoryId, String categoryCode, String categoryName) {
        return new Category(categoryId, new CategoryCode(categoryCode), categoryName);
    }

    public String getCategoryCode() {
        return categoryCode.value();
    }
}