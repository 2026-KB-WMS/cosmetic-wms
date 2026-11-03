package com.kb.cosmetic_wms.oms.adapter.out.persistence;

import com.kb.cosmetic_wms.oms.domain.enums.FailedEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface FailedAssignmentEventJpaRepository extends JpaRepository<FailedAssignmentEventEntity, Long> {

    List<FailedAssignmentEventEntity> findByStatusOrderByFailedAtAsc(FailedEventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM FailedAssignmentEventEntity e WHERE e.id = :id")
    Optional<FailedAssignmentEventEntity> findByIdForUpdate(@Param("id") Long id);
}
