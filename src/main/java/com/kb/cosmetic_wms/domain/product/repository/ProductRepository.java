package com.kb.cosmetic_wms.domain.product.repository;

import com.kb.cosmetic_wms.domain.product.entity.Category;
import com.kb.cosmetic_wms.domain.product.entity.Product;
import com.kb.cosmetic_wms.domain.product.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT COALESCE(COUNT(p), 0) + 1 FROM Product p " +
           "WHERE p.category.categoryCode = :categoryCode " +
           "AND p.productType.typeCode = :typeCode " +
           "AND p.productInfo.volume.value = :volume " +
           "AND p.brandName = :brandName")
    int findNextSequence(@Param("categoryCode") String categoryCode,
                         @Param("typeCode") String typeCode,
                         @Param("volume") int volume,
                         @Param("brandName") String brandName);

    @Query("SELECT COUNT(p) > 0 FROM Product p " +
           "WHERE p.brandName = :brandName " +
           "AND p.productName = :productName " +
           "AND p.category = :category " +
           "AND p.productType = :productType " +
           "AND p.productInfo.volume.value = :volumeValue " +
           "AND p.productInfo.volume.unit = :volumeUnit")
    boolean existsDuplicateProduct(@Param("brandName") String brandName,
                                   @Param("productName") String productName,
                                   @Param("category") Category category,
                                   @Param("productType") ProductType productType,
                                   @Param("volumeValue") int volumeValue,
                                   @Param("volumeUnit") String volumeUnit);
}
