package com.kb.cosmetic_wms.partner.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PartnerJpaRepository extends JpaRepository<PartnerEntity, Long> {
    boolean existsByBusinessNumber(String businessNumber);
}