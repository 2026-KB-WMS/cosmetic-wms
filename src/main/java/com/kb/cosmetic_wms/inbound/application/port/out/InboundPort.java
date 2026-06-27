package com.kb.cosmetic_wms.inbound.application.port.out;

import com.kb.cosmetic_wms.inbound.domain.model.Inbound;

import java.util.Optional;

public interface InboundPort {

    Optional<Inbound> findByIdWithLines(Long id);

    Optional<Inbound> findByIdWithLinesForUpdate(Long id);

    Inbound save(Inbound inbound);
}