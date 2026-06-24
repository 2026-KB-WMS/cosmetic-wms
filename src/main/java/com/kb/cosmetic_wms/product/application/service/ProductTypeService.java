package com.kb.cosmetic_wms.product.application.service;

import com.kb.cosmetic_wms.product.application.port.in.*;
import com.kb.cosmetic_wms.product.application.port.out.ProductTypePort;
import com.kb.cosmetic_wms.product.domain.exception.DuplicateProductTypeException;
import com.kb.cosmetic_wms.product.domain.exception.ProductTypeNotFoundException;
import com.kb.cosmetic_wms.product.domain.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeService implements RegisterProductTypeUseCase, FindProductTypeUseCase, DeleteProductTypeUseCase {

    private final ProductTypePort productTypePort;

    @Override
    @Transactional
    public ProductTypeResult register(RegisterProductTypeCommand command) {
        if (productTypePort.existsByCode(command.typeCode())) {
            throw new DuplicateProductTypeException();
        }
        ProductType productType = ProductType.create(command.typeCode(), command.typeName());
        return ProductTypeResult.from(productTypePort.save(productType));
    }

    @Override
    public List<ProductTypeResult> findAll() {
        return productTypePort.findAll().stream()
                .map(ProductTypeResult::from)
                .toList();
    }

    @Override
    public ProductTypeResult findById(Long productTypeId) {
        ProductType productType = productTypePort.findById(productTypeId)
                .orElseThrow(ProductTypeNotFoundException::new);
        return ProductTypeResult.from(productType);
    }

    @Override
    @Transactional
    public void delete(Long productTypeId) {
        productTypePort.findById(productTypeId)
                .orElseThrow(ProductTypeNotFoundException::new);
        productTypePort.deleteById(productTypeId);
    }
}