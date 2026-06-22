package com.kb.cosmetic_wms.domain.inspection.repository;

import com.kb.cosmetic_wms.domain.inspection.entity.QualityInspection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InspectionRepository extends JpaRepository<QualityInspection, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QualityInspection q WHERE q.id = :id")
    Optional<QualityInspection> findByIdForUpdate(@Param("id") Long id);
}
