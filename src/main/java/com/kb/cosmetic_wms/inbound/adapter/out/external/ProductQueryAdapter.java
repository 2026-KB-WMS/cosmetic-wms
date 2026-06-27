package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.ProductQueryPort;
import com.kb.cosmetic_wms.product.product.application.port.out.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductQueryAdapter implements ProductQueryPort {

    private final ProductPort productPort;

    @Override
    public boolean existsById(Long productId) {
        return productPort.existsById(productId);
    }
}
