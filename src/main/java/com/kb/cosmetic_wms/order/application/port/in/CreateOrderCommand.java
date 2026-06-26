package com.kb.cosmetic_wms.order.application.port.in;

import com.kb.cosmetic_wms.order.domain.model.OrderLine;

import java.util.List;

public record CreateOrderCommand(Long storeId, Long warehouseId, List<OrderLine> lines) {}