package com.kb.cosmetic_wms.lot.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface LotJpaRepository extends JpaRepository<LotEntity, Long> {
    boolean existsByInboundIdAndManufacturerLotNumber(Long inboundId, String manufacturerLotNumber);
    List<LotEntity> findByProductId(Long productId);
}
