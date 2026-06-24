package com.kb.cosmetic_wms.domain.lot.service;

import com.kb.cosmetic_wms.domain.lot.dto.LotCreateRequestDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotDetailResponseDto;
import com.kb.cosmetic_wms.domain.lot.dto.LotStatusUpdateRequestDto;
import com.kb.cosmetic_wms.domain.lot.entity.Lot;
import com.kb.cosmetic_wms.domain.lot.exception.DuplicateLotNumberException;
import com.kb.cosmetic_wms.domain.lot.exception.LotNotFoundException;
import com.kb.cosmetic_wms.domain.lot.exception.LotProductNotFoundException;
import com.kb.cosmetic_wms.domain.lot.repository.LotRepository;
import com.kb.cosmetic_wms.product.application.port.in.FindProductUseCase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LotService {

    private final LotRepository lotRepository;
    private final FindProductUseCase findProductUseCase;

    @Transactional
    public LotDetailResponseDto register(LotCreateRequestDto request) {
        if (!findProductUseCase.existsById(request.productId())) {
            throw new LotProductNotFoundException();
        }

        if (lotRepository.existsByLotNumber(request.lotNumber())) {
            throw new DuplicateLotNumberException();
        }

        Lot lot = Lot.create(
                request.lotNumber(),
                request.manufacturingDate(),
                request.expirationDate(),
                request.productId()
        );

        return LotDetailResponseDto.from(lotRepository.save(lot));
    }

    public LotDetailResponseDto getLot(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(LotNotFoundException::new);
        return LotDetailResponseDto.from(lot);
    }

    public List<LotDetailResponseDto> getLotsByProductId(Long productId) {
        if (!findProductUseCase.existsById(productId)) {
            throw new LotProductNotFoundException();
        }

        return lotRepository.findByProductId(productId).stream()
                .map(LotDetailResponseDto::from)
                .toList();
    }

    @Transactional
    public LotDetailResponseDto updateLotStatus(Long lotId, LotStatusUpdateRequestDto request) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(LotNotFoundException::new);
        lot.changeStatus(request.status());
        return LotDetailResponseDto.from(lot);
    }

    @Transactional
    public void deleteLot(Long lotId) {
        Lot lot = lotRepository.findById(lotId)
                .orElseThrow(LotNotFoundException::new);
        lotRepository.delete(lot);
    }
}
