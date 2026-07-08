package com.kb.cosmetic_wms.partner.application.service;

import com.kb.cosmetic_wms.partner.application.port.in.FindPartnerUseCase;
import com.kb.cosmetic_wms.partner.application.port.in.PartnerResult;
import com.kb.cosmetic_wms.partner.application.port.in.RegisterPartnerCommand;
import com.kb.cosmetic_wms.partner.application.port.in.RegisterPartnerUseCase;
import com.kb.cosmetic_wms.partner.application.port.out.PartnerPort;
import com.kb.cosmetic_wms.partner.domain.exception.DuplicatePartnerException;
import com.kb.cosmetic_wms.partner.domain.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.partner.domain.model.Partner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnerService implements RegisterPartnerUseCase, FindPartnerUseCase {

    private final PartnerPort partnerPort;

    @Override
    @Transactional
    public PartnerResult register(RegisterPartnerCommand command) {
        if (partnerPort.existsByBusinessNumber(command.businessNumber())) {
            throw new DuplicatePartnerException();
        }

        Partner partner = Partner.create(command.name(), command.type(), command.businessNumber());
        return PartnerResult.from(partnerPort.save(partner));
    }

    @Override
    public PartnerResult findById(Long partnerId) {
        Partner partner = partnerPort.findById(partnerId)
                .orElseThrow(PartnerNotFoundException::new);
        return PartnerResult.from(partner);
    }

    @Override
    public boolean existsById(Long partnerId) {
        return partnerPort.existsById(partnerId);
    }
}