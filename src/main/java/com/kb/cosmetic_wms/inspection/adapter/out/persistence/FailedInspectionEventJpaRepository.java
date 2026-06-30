package com.kb.cosmetic_wms.inspection.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface FailedInspectionEventJpaRepository extends JpaRepository<FailedInspectionEventEntity, Long> {
}