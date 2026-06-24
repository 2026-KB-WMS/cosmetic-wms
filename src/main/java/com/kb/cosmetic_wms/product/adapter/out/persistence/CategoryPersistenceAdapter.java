package com.kb.cosmetic_wms.product.adapter.out.persistence;

import com.kb.cosmetic_wms.product.application.port.out.CategoryPort;
import com.kb.cosmetic_wms.product.domain.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}