package com.kb.cosmetic_wms.domain.order.repository;

import com.kb.cosmetic_wms.domain.order.entity.Orders;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orders o WHERE o.id = :id")
    Optional<Orders> findByIdForUpdate(@Param("id") Long id);

    // confirmOrder: items를 JOIN FETCH로 로드하면서 orders 행 잠금 획득
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Orders o JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Orders> findByIdWithOrderItemsForUpdate(@Param("id") Long id);
}