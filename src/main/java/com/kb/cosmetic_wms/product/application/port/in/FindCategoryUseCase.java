package com.kb.cosmetic_wms.product.application.port.in;

import java.util.List;

public interface FindCategoryUseCase {
    List<CategoryResult> findAll();
    CategoryResult findById(Long categoryId);
}