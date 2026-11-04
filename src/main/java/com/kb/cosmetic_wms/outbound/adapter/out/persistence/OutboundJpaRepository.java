package com.kb.cosmetic_wms.outbound.adapter.out.persistence;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface OutboundJpaRepository extends JpaRepository<OutboundEntity, Long> {

    List<OutboundEntity> findByIdInAndOutboundStatusInOrderByIdAsc(Collection<Long> ids, Collection<OutboundStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboundEntity o WHERE o.id = :id")
    Optional<OutboundEntity> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OutboundEntity o JOIN FETCH o.outboundItems WHERE o.id = :id")
    Optional<OutboundEntity> findByIdWithItemsForUpdate(@Param("id") Long id);

    @Query("SELECT o FROM OutboundEntity o JOIN FETCH o.outboundItems WHERE o.id = :id")
    Optional<OutboundEntity> findByIdWithItems(@Param("id") Long id);
}
