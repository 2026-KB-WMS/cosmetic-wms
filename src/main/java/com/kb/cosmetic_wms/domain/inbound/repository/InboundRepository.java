package com.kb.cosmetic_wms.domain.inbound.repository;

import com.kb.cosmetic_wms.domain.inbound.entity.Inbound;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InboundRepository extends JpaRepository<Inbound, Long> {

    @Query("SELECT i FROM Inbound i JOIN FETCH i.inboundItems WHERE i.id = :id")
    Optional<Inbound> findByIdWithItems(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inbound i WHERE i.id = :id")
    Optional<Inbound> findByIdForUpdate(@Param("id") Long id);

    // JOIN FETCH + PESSIMISTIC_WRITE: completeInbound에서 items를 한 번에 로드하면서 행 잠금 획득
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inbound i JOIN FETCH i.inboundItems WHERE i.id = :id")
    Optional<Inbound> findByIdWithItemsForUpdate(@Param("id") Long id);
}
