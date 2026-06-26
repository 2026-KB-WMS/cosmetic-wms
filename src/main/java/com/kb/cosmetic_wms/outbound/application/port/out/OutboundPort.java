package com.kb.cosmetic_wms.outbound.application.port.out;

import com.kb.cosmetic_wms.outbound.domain.model.Outbound;

import java.util.Optional;

public interface OutboundPort {

    Optional<Outbound> findByIdForUpdate(Long id);

    Optional<Outbound> findByIdWithItemsForUpdate(Long id);

    Outbound save(Outbound outbound);
}