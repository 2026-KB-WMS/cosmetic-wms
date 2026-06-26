package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface OutboundJpaRepository extends JpaRepository<OutboundEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboundEntity o WHERE o.id = :id")
    Optional<OutboundEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboundEntity o JOIN FETCH o.outboundItems WHERE o.id = :id")
    Optional<OutboundEntity> findByIdWithItemsForUpdate(@Param("id") Long id);
}