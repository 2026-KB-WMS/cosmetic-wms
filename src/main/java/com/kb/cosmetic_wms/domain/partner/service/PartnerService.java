package com.kb.cosmetic_wms.domain.partner.service;

import com.kb.cosmetic_wms.domain.partner.dto.PartnerCreateRequestDto;
import com.kb.cosmetic_wms.domain.partner.dto.PartnerResponseDto;
import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import com.kb.cosmetic_wms.domain.partner.exception.DuplicatePartnerException;
import com.kb.cosmetic_wms.domain.partner.exception.PartnerNotFoundException;
import com.kb.cosmetic_wms.domain.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    @Transactional
    public PartnerResponseDto register(PartnerCreateRequestDto requestDto) {
        if (partnerRepository.existsByBusinessNumber(requestDto.businessNumber())) {
            throw new DuplicatePartnerException();
        }

        Partner partner = Partner.create(
                requestDto.partnerName(),
                requestDto.partnerType(),
                requestDto.businessNumber()
        );

        Partner savedPartner = partnerRepository.save(partner);
        return PartnerResponseDto.from(savedPartner);
    }

    public PartnerResponseDto findById(Long partnerId) {
        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(PartnerNotFoundException::new);
        return PartnerResponseDto.from(partner);
    }
}
