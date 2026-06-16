package com.kb.cosmetic_wms.domain.partner.repository;

import com.kb.cosmetic_wms.domain.partner.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    boolean existsByBusinessNumber(String businessNumber);
}
