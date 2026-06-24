package com.kb.cosmetic_wms.product.application.port.out;

import com.kb.cosmetic_wms.product.domain.model.Category;

import java.util.Optional;

public interface CategoryPort {
    Optional<Category> findById(Long categoryId);
}