package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {

    private final FindProductUseCase findProductUseCase;

    @Override
    public boolean existsById(Long productId) {
        return findProductUseCase.existsById(productId);
    }

    @Override
    public boolean allExistByIds(Collection<Long> productIds) {
        return findProductUseCase.allExistByIds(productIds);
    }
}
