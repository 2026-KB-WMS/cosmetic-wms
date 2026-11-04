package com.kb.cosmetic_wms.outbound.application.port.out;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.model.Outbound;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OutboundPort {

    Optional<Outbound> findByIdForUpdate(Long id);

    Optional<Outbound> findByIdWithItemsForUpdate(Long id);

    List<Outbound> findByIdsAndStatuses(Collection<Long> ids, Collection<OutboundStatus> statuses);

    Outbound save(Outbound outbound);
}