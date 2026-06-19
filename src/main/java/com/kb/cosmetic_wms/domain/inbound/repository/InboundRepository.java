package com.kb.cosmetic_wms.domain.inbound.repository;

import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

    @Query("SELECT i FROM Inbound i JOIN FETCH i.inboundItems WHERE i.id = :id")
    Optional<Inbound> findByIdWithItems(@Param("id") Long id);
}
