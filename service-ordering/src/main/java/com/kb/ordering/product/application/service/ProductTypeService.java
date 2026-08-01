package com.kb.ordering.product.application.service;

import com.kb.ordering.product.application.port.in.DeleteProductTypeUseCase;
import com.kb.ordering.product.application.port.in.FindProductTypeUseCase;
import com.kb.ordering.product.application.port.in.RegisterProductTypeUseCase;
import com.kb.ordering.product.application.port.in.dto.ProductTypeResult;
import com.kb.ordering.product.application.port.in.dto.RegisterProductTypeCommand;
import com.kb.ordering.product.application.port.out.ProductPort;
import com.kb.ordering.product.application.port.out.ProductTypePort;
import com.kb.ordering.product.domain.producttype.exception.DuplicateProductTypeException;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeInUseException;
import com.kb.ordering.product.domain.producttype.exception.ProductTypeNotFoundException;
import com.kb.ordering.product.domain.producttype.model.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductTypeService implements RegisterProductTypeUseCase, FindProductTypeUseCase, DeleteProductTypeUseCase {

    private final ProductTypePort productTypePort;
    private final ProductPort productPort;

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
        if (productPort.existsByProductTypeId(productTypeId)) {
            throw new ProductTypeInUseException();
        }
        productTypePort.deleteById(productTypeId);
    }
}
