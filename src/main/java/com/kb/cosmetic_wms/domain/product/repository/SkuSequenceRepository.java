package com.kb.cosmetic_wms.domain.product.repository;

import com.kb.cosmetic_wms.domain.product.entity.SkuSequence;
import com.kb.cosmetic_wms.domain.product.entity.SkuSequenceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SkuSequenceRepository extends JpaRepository<SkuSequence, SkuSequenceId> {

    @Modifying
    @Transactional
    @Query(value = "INSERT IGNORE INTO sku_sequence (brand_name, category_code, type_code, volume, current_seq) " +
                   "VALUES (:brandName, :categoryCode, :typeCode, :volume, 0)",
           nativeQuery = true)
    void initIfAbsent(@Param("brandName") String brandName,
                      @Param("categoryCode") String categoryCode,
                      @Param("typeCode") String typeCode,
                      @Param("volume") int volume);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SkuSequence s WHERE s.id.brandName = :brandName " +
           "AND s.id.categoryCode = :categoryCode " +
           "AND s.id.typeCode = :typeCode " +
           "AND s.id.volume = :volume")
    Optional<SkuSequence> findForUpdate(@Param("brandName") String brandName,
                                        @Param("categoryCode") String categoryCode,
                                        @Param("typeCode") String typeCode,
                                        @Param("volume") int volume);
}
