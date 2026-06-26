package com.kb.cosmetic_wms.partner.adapter.out.persistence;

import com.kb.cosmetic_wms.partner.application.port.out.PartnerPort;
import com.kb.cosmetic_wms.partner.domain.model.Partner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PartnerPersistenceAdapter implements PartnerPort {

    private final PartnerJpaRepository partnerJpaRepository;

    @Override
    public boolean existsByBusinessNumber(String businessNumber) {
        return partnerJpaRepository.existsByBusinessNumber(businessNumber);
    }

    @Override
    public Optional<Partner> findById(Long partnerId) {
        return partnerJpaRepository.findById(partnerId)
                .map(PartnerEntity::toDomain);
    }

    @Override
    public Partner save(Partner partner) {
        return partnerJpaRepository.save(PartnerEntity.fromDomain(partner)).toDomain();
    }
}