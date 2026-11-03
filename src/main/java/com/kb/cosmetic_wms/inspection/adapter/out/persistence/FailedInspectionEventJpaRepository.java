package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import com.kb.cosmetic_wms.inspection.domain.enums.FailedEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface FailedInspectionEventJpaRepository extends JpaRepository<FailedInspectionEventEntity, Long> {

    List<FailedInspectionEventEntity> findByStatusOrderByFailedAtAsc(FailedEventStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM FailedInspectionEventEntity e WHERE e.id = :id")
    Optional<FailedInspectionEventEntity> findByIdForUpdate(@Param("id") Long id);
}
