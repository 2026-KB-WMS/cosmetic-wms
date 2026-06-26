package com.kb.cosmetic_wms.product.category.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_code", nullable = false, columnDefinition = "CHAR(3)")
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    private CategoryEntity(String categoryCode, String categoryName) {
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
    }

    public static CategoryEntity fromDomain(Category category) {
        return new CategoryEntity(category.getCategoryCode(), category.getCategoryName());
    }

    public Category toDomain() {
        return Category.reconstitute(id, categoryCode, categoryName);
    }
}