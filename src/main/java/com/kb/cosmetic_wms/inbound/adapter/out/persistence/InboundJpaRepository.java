package com.kb.cosmetic_wms.inbound.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface InboundJpaRepository extends JpaRepository<InboundEntity, Long> {

    @Query("SELECT i FROM InboundEntity i JOIN FETCH i.inboundLines WHERE i.id = :id")
    Optional<InboundEntity> findByIdWithLines(@Param("id") Long id);

    // FOR UPDATE를 inbound 단독으로 걸어 inbound_line 쪽에 gap lock이 생기지 않게 한다.
    // JOIN FETCH + FOR UPDATE 조합은 inbound_line의 next-key lock을 유발해 concurrent INSERT와 Deadlock을 일으킨다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InboundEntity i WHERE i.id = :id")
    Optional<InboundEntity> findByIdForUpdate(@Param("id") Long id);
}