package com.kb.cosmetic_wms.domain.product.repository;

import com.kb.cosmetic_wms.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
