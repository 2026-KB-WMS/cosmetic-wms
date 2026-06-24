package com.kb.cosmetic_wms.product.application.port.out;

import com.kb.cosmetic_wms.product.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryPort {
    Optional<Category> findById(Long categoryId);
    List<Category> findAll();
    boolean existsByCode(String categoryCode);
    Category save(Category category);
    void deleteById(Long categoryId);
}