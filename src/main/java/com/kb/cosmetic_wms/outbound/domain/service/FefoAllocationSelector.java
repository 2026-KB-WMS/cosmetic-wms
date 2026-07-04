package com.kb.cosmetic_wms.outbound.domain.service;

import com.kb.cosmetic_wms.outbound.domain.exception.OutboundInsufficientStockException;
import com.kb.cosmetic_wms.outbound.domain.model.AvailableStock;
import com.kb.cosmetic_wms.outbound.domain.model.OutboundLine;

import java.util.ArrayList;
import java.util.List;

/**
 * 선유통기한 선출고(FEFO) 원칙에 따라 요구 수량을 가용 재고 조각에 배분한다.
 * 재고 조각은 유통기한 임박 순으로 정렬되어 있다고 가정한다.
 */
public class FefoAllocationSelector {

    public List<OutboundLine> select(Long orderItemId, int requiredQuantity, List<AvailableStock> stocks) {
        List<OutboundLine> lines = new ArrayList<>();
        int remaining = requiredQuantity;

        for (AvailableStock stock : stocks) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(remaining, stock.availableQuantity());
            lines.add(new OutboundLine(orderItemId, stock.inventoryId(), take));
            remaining -= take;
        }

        if (remaining > 0) {
            throw new OutboundInsufficientStockException();
        }
        return lines;
    }
}
