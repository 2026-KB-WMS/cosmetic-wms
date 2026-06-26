package com.kb.cosmetic_wms.inbound.application.port.out;

import com.kb.cosmetic_wms.inbound.domain.model.Inbound;

import java.util.Optional;

public interface InboundPort {

    Optional<Inbound> findByIdWithItems(Long id);

    Optional<Inbound> findByIdWithItemsForUpdate(Long id);

    Inbound save(Inbound inbound);
}
