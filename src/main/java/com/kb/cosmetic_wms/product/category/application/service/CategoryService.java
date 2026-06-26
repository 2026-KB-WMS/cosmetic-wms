package com.kb.cosmetic_wms.product.category.application.service;

import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import com.kb.cosmetic_wms.product.category.application.port.in.*;
import com.kb.cosmetic_wms.product.category.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryInUseException;
import com.kb.cosmetic_wms.product.category.domain.exception.CategoryNotFoundException;
import com.kb.cosmetic_wms.product.category.domain.exception.DuplicateCategoryException;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService implements RegisterCategoryUseCase, FindCategoryUseCase, DeleteCategoryUseCase {

    private final CategoryPort categoryPort;
    private final ProductPort productPort;

    @Override
    @Transactional
    public CategoryResult register(RegisterCategoryCommand command) {
        if (categoryPort.existsByCode(command.categoryCode())) {
            throw new DuplicateCategoryException();
        }
        Category category = Category.create(command.categoryCode(), command.categoryName());
        return CategoryResult.from(categoryPort.save(category));
    }

    @Override
    public List<CategoryResult> findAll() {
        return categoryPort.findAll().stream()
                .map(CategoryResult::from)
                .toList();
    }

    @Override
    public CategoryResult findById(Long categoryId) {
        Category category = categoryPort.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        return CategoryResult.from(category);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        categoryPort.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
        if (productPort.existsByCategoryId(categoryId)) {
            throw new CategoryInUseException();
        }
        categoryPort.deleteById(categoryId);
    }
}