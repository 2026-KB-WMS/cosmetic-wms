package com.kb.cosmetic_wms.inventory.application.service;

import com.kb.cosmetic_wms.inventory.application.port.in.ManageLotBlockUseCase;
import com.kb.cosmetic_wms.inventory.application.port.out.LotBlockPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LotBlockService implements ManageLotBlockUseCase {

    private final LotBlockPort lotBlockPort;

    @Override
    public void block(Long lotId, String status) {
        lotBlockPort.block(lotId, status);
    }

    @Override
    public void release(Long lotId) {
        lotBlockPort.release(lotId);
    }
}
