package com.kb.cosmetic_wms.lot.application.service;

import com.kb.cosmetic_wms.lot.application.port.in.*;
import com.kb.cosmetic_wms.lot.application.port.out.LotPort;
import com.kb.cosmetic_wms.lot.domain.exception.DuplicateLotNumberException;
import com.kb.cosmetic_wms.lot.domain.exception.LotNotFoundException;
import com.kb.cosmetic_wms.lot.domain.exception.LotProductNotFoundException;
import com.kb.cosmetic_wms.lot.domain.model.Lot;
import com.kb.cosmetic_wms.product.product.application.port.in.FindProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LotService implements RegisterLotUseCase, FindLotUseCase,
        UpdateLotStatusUseCase, DeleteLotUseCase {

    private final LotPort lotPort;
    private final FindProductUseCase findProductUseCase;

    @Override
    @Transactional
    public LotResult register(RegisterLotCommand command) {
        if (!findProductUseCase.existsById(command.productId())) {
            throw new LotProductNotFoundException();
        }

        Lot lot = Lot.create(
                command.inboundId(),
                command.manufacturerLotNumber(),
                command.manufacturingDate(),
                command.expirationDate(),
                command.productId()
        );

        if (lotPort.existsByInboundIdAndManufacturerLotNumber(lot.getInboundId(), lot.getManufacturerLotNumber())) {
            throw new DuplicateLotNumberException();
        }
        return LotResult.from(lotPort.save(lot));
    }

    @Override
    public LotResult findById(Long lotId) {
        return lotPort.findById(lotId)
                .map(LotResult::from)
                .orElseThrow(LotNotFoundException::new);
    }

    @Override
    public List<LotResult> findByProductId(Long productId) {
        if (!findProductUseCase.existsById(productId)) {
            throw new LotProductNotFoundException();
        }
        return lotPort.findByProductId(productId).stream()
                .map(LotResult::from)
                .toList();
    }

    @Override
    @Transactional
    public LotResult updateStatus(Long lotId, UpdateLotStatusCommand command) {
        Lot lot = lotPort.findById(lotId)
                .orElseThrow(LotNotFoundException::new);
        lot.changeStatus(command.status());
        return LotResult.from(lotPort.save(lot));
    }

    @Override
    @Transactional
    public void delete(Long lotId) {
        Lot lot = lotPort.findById(lotId)
                .orElseThrow(LotNotFoundException::new);
        lotPort.delete(lot);
    }
}
