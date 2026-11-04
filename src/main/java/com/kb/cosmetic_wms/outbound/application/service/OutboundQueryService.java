package com.kb.cosmetic_wms.outbound.application.service;

import com.kb.cosmetic_wms.outbound.application.port.in.FindOutboundUseCase;
import com.kb.cosmetic_wms.outbound.application.port.in.OutboundResult;
import com.kb.cosmetic_wms.outbound.application.port.out.LotAllocationQueryPort;
import com.kb.cosmetic_wms.outbound.application.port.out.OutboundPort;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OutboundQueryService implements FindOutboundUseCase {

    private final LotAllocationQueryPort lotAllocationQueryPort;
    private final OutboundPort outboundPort;

    @Override
    public List<OutboundResult> findInProgressByLotId(Long lotId) {
        List<Long> allocatedOutboundIds = lotAllocationQueryPort.findAllocatedOutboundIds(lotId);
        if (allocatedOutboundIds.isEmpty()) {
            return List.of();
        }
        return outboundPort.findByIdsAndStatuses(allocatedOutboundIds, OutboundStatus.holdingAllocatedStock()).stream()
                .map(OutboundResult::from)
                .toList();
    }
}
