package com.kb.cosmetic_wms.putaway.application.service;

import com.kb.cosmetic_wms.putaway.application.port.in.CreatePutawayOrderCommand;
import com.kb.cosmetic_wms.putaway.application.port.in.CreatePutawayOrderUseCase;
import com.kb.cosmetic_wms.putaway.application.port.in.FindPutawayOrderUseCase;
import com.kb.cosmetic_wms.putaway.application.port.in.PutawayOrderResult;
import com.kb.cosmetic_wms.putaway.application.port.out.PutawayOrderPort;
import com.kb.cosmetic_wms.putaway.application.port.out.StorageSectionQueryPort;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayOrderNotFoundException;
import com.kb.cosmetic_wms.putaway.domain.exception.PutawayTargetSectionNotFoundException;
import com.kb.cosmetic_wms.putaway.domain.model.PutawayOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutawayOrderService implements CreatePutawayOrderUseCase, FindPutawayOrderUseCase {

    private final PutawayOrderPort putawayOrderPort;
    private final StorageSectionQueryPort storageSectionQueryPort;

    @Override
    @Transactional
    public List<PutawayOrderResult> create(CreatePutawayOrderCommand command) {
        if (command.passedQuantity() <= 0 && command.failedQuantity() <= 0) {
            return List.of();
        }

        Long dockingSectionId = storageSectionQueryPort.findDockingSectionId(
                command.warehouseId(), command.productId());

        List<PutawayOrderResult> results = new ArrayList<>();

        if (command.passedQuantity() > 0) {
            Long storageSectionId = storageSectionQueryPort
                    .findAvailableStorageSectionId(command.warehouseId(), command.productId())
                    .orElseThrow(PutawayTargetSectionNotFoundException::new);

            PutawayOrder order = PutawayOrder.create(
                    command.inspectionId(), command.lotId(), command.productId(),
                    command.warehouseId(), dockingSectionId, storageSectionId,
                    command.passedQuantity());
            results.add(PutawayOrderResult.from(putawayOrderPort.save(order)));
        }

        if (command.failedQuantity() > 0) {
            Long quarantineSectionId = storageSectionQueryPort
                    .findAvailableQuarantineSectionId(command.warehouseId())
                    .orElseThrow(PutawayTargetSectionNotFoundException::new);

            PutawayOrder order = PutawayOrder.create(
                    command.inspectionId(), command.lotId(), command.productId(),
                    command.warehouseId(), dockingSectionId, quarantineSectionId,
                    command.failedQuantity());
            results.add(PutawayOrderResult.from(putawayOrderPort.save(order)));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public PutawayOrderResult findById(Long putawayOrderId) {
        return putawayOrderPort.findById(putawayOrderId)
                .map(PutawayOrderResult::from)
                .orElseThrow(PutawayOrderNotFoundException::new);
    }
}
