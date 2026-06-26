package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface InspectionJpaRepository extends JpaRepository<InspectionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM InspectionEntity q WHERE q.id = :id")
    Optional<InspectionEntity> findByIdForUpdate(@Param("id") Long id);
}