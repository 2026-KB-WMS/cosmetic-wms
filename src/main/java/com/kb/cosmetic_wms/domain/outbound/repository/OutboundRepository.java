package com.kb.cosmetic_wms.domain.outbound.repository;

import com.kb.cosmetic_wms.domain.outbound.entity.Outbound;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OutboundRepository extends JpaRepository<Outbound, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Outbound o WHERE o.id = :id")
    Optional<Outbound> findByIdForUpdate(@Param("id") Long id);
}