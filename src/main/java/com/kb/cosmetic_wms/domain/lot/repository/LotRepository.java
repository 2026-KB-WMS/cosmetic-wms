package com.kb.cosmetic_wms.domain.lot.repository;

import com.kb.cosmetic_wms.domain.lot.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LotRepository extends JpaRepository<Lot, Long> {

    boolean existsByLotNumber(String lotNumber);

    List<Lot> findByProductId(Long productId);
}
