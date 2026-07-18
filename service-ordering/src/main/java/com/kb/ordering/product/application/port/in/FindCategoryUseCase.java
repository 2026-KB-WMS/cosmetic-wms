package com.kb.ordering.product.application.port.in;

import com.kb.ordering.product.application.port.in.dto.CategoryResult;

import java.util.List;

public interface FindCategoryUseCase {
    List<CategoryResult> findAll();

    CategoryResult findById(Long categoryId);
}