package com.kb.ordering.product.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    long countByIdIn(Collection<Long> ids);

    @Query("SELECT p.id, p.temperatureType FROM ProductEntity p WHERE p.id IN :ids")
    List<Object[]> findIdAndTemperatureTypeByIdIn(@Param("ids") Collection<Long> ids);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByProductTypeId(Long productTypeId);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p " +
            "WHERE p.brandName = :brandName " +
            "AND p.productName = :productName " +
            "AND p.categoryId = :categoryId " +
            "AND p.productTypeId = :productTypeId " +
            "AND p.volumeValue = :volumeValue " +
            "AND p.volumeUnit = :volumeUnit")
    boolean existsDuplicateProduct(@Param("brandName") String brandName,
                                   @Param("productName") String productName,
                                   @Param("categoryId") Long categoryId,
                                   @Param("productTypeId") Long productTypeId,
                                   @Param("volumeValue") int volumeValue,
                                   @Param("volumeUnit") String volumeUnit);
}