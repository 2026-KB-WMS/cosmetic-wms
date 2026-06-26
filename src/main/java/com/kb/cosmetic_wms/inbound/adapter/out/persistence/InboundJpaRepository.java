package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface InboundJpaRepository extends JpaRepository<InboundEntity, Long> {

    @Query("SELECT i FROM InboundEntity i JOIN FETCH i.inboundItems WHERE i.id = :id")
    Optional<InboundEntity> findByIdWithItems(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InboundEntity i JOIN FETCH i.inboundItems WHERE i.id = :id")
    Optional<InboundEntity> findByIdWithItemsForUpdate(@Param("id") Long id);
}
