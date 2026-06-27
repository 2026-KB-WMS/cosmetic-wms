package com.kb.cosmetic_wms.product.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.category JOIN FETCH p.productType")
    List<ProductEntity> findAllWithDetails();

    @Query("SELECT p FROM ProductEntity p JOIN FETCH p.category JOIN FETCH p.productType WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithDetails(@Param("id") Long id);

    long countByIdIn(Collection<Long> ids);

    boolean existsByCategory_Id(Long categoryId);

    boolean existsByProductType_Id(Long productTypeId);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p " +
            "WHERE p.brandName = :brandName " +
            "AND p.productName = :productName " +
            "AND p.category.id = :categoryId " +
            "AND p.productType.id = :productTypeId " +
            "AND p.volumeValue = :volumeValue " +
            "AND p.volumeUnit = :volumeUnit")
    boolean existsDuplicateProduct(@Param("brandName") String brandName,
                                   @Param("productName") String productName,
                                   @Param("categoryId") Long categoryId,
                                   @Param("productTypeId") Long productTypeId,
                                   @Param("volumeValue") int volumeValue,
                                   @Param("volumeUnit") String volumeUnit);
}