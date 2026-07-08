package com.kb.cosmetic_wms.lot.adapter.out.external;

import com.kb.cosmetic_wms.lot.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotProductQueryAdapter implements ProductQueryPort {

    private final FindProductUseCase findProductUseCase;

    @Override
    public boolean existsById(Long productId) {
        return findProductUseCase.existsById(productId);
    }
}
