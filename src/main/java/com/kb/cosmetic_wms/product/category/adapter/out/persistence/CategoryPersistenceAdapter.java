package com.kb.cosmetic_wms.product.category.adapter.out.persistence;

import com.kb.cosmetic_wms.product.category.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.category.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryPort {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .map(CategoryEntity::toDomain);
    }

    @Override
    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(CategoryEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCode(String categoryCode) {
        return categoryJpaRepository.existsByCategoryCode(categoryCode);
    }

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(CategoryEntity.fromDomain(category)).toDomain();
    }

    @Override
    public void deleteById(Long categoryId) {
        categoryJpaRepository.deleteById(categoryId);
    }
}