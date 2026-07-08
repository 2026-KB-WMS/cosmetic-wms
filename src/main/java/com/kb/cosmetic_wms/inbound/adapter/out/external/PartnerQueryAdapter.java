package com.kb.cosmetic_wms.inbound.adapter.out.external;

import com.kb.cosmetic_wms.inbound.application.port.out.PartnerQueryPort;
import com.kb.cosmetic_wms.partner.application.port.in.FindPartnerUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartnerQueryAdapter implements PartnerQueryPort {

    private final FindPartnerUseCase findPartnerUseCase;

    @Override
    public boolean existsById(Long partnerId) {
        return findPartnerUseCase.existsById(partnerId);
    }
}
