package com.kb.cosmetic_wms.outbound.application.port.out;

import java.util.List;

public interface LotAllocationQueryPort {

    List<Long> findAllocatedOutboundIds(Long lotId);
}
