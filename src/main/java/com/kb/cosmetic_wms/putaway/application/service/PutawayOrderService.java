package com.kb.cosmetic_wms.putaway.application.service;

import com.kb.cosmetic_wms.global.event.EventPublisher;
import com.kb.cosmetic_wms.putaway.domain.event.PutawayCompletedEvent;
import com.kb.cosmetic_wms.putaway.application.port.in.CompletePutawayUseCase;
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
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PutawayOrderService implements CreatePutawayOrderUseCase, FindPutawayOrderUseCase, CompletePutawayUseCase {

    private final PutawayOrderPort putawayOrderPort;
    private final StorageSectionQueryPort storageSectionQueryPort;
    private final EventPublisher eventPublisher;
    private final AuditorAware<Long> auditorProvider;

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
                    command.passedQuantity(), true);
            results.add(PutawayOrderResult.from(putawayOrderPort.save(order)));
        }

        if (command.failedQuantity() > 0) {
            Long quarantineSectionId = storageSectionQueryPort
                    .findAvailableQuarantineSectionId(command.warehouseId())
                    .orElseThrow(PutawayTargetSectionNotFoundException::new);

            PutawayOrder order = PutawayOrder.create(
                    command.inspectionId(), command.lotId(), command.productId(),
                    command.warehouseId(), dockingSectionId, quarantineSectionId,
                    command.failedQuantity(), false);
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

    @Override
    @Transactional
    public PutawayOrderResult complete(Long putawayOrderId) {
        Long memberId = auditorProvider.getCurrentAuditor().orElseThrow();
        PutawayOrder order = putawayOrderPort.findByIdForUpdate(putawayOrderId)
                .orElseThrow(PutawayOrderNotFoundException::new);
        order.complete();
        PutawayOrder saved = putawayOrderPort.save(order);
        eventPublisher.publish(new PutawayCompletedEvent(
                saved.getId(),
                saved.getLotId(),
                saved.getProductId(),
                saved.getWarehouseId(),
                saved.getSourceSectionId(),
                saved.getTargetSectionId(),
                saved.getQuantity(),
                saved.isNormalQuality(),
                memberId
        ));
        return PutawayOrderResult.from(saved);
    }
}
