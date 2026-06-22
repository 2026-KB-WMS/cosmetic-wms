package com.kb.cosmetic_wms.domain.order.repository;

import com.kb.cosmetic_wms.domain.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {
}