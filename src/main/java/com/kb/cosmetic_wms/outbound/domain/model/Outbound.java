package com.kb.cosmetic_wms.outbound.domain.model;

import com.kb.cosmetic_wms.outbound.domain.enums.OutboundStatus;
import com.kb.cosmetic_wms.outbound.domain.enums.OutboundType;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundIncompletePickingException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundItemNotFoundException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundItemRequiredException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundOrderIdRequiredException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundTypeRequiredException;
import com.kb.cosmetic_wms.outbound.domain.exception.OutboundWarehouseRequiredException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Outbound {

    private Long id;
    private Long ordersId;
    private Long warehouseId;
    private OutboundType outboundType;
    private OutboundStatus outboundStatus;
    private LocalDateTime outboundDate;
    private final List<OutboundItem> outboundItems;

    private Outbound(Long ordersId, Long warehouseId, OutboundType outboundType) {
        this.ordersId = ordersId;
        this.warehouseId = warehouseId;
        this.outboundType = outboundType;
        this.outboundStatus = OutboundStatus.PENDING;
        this.outboundDate = null;
        this.outboundItems = new ArrayList<>();
    }

    private Outbound() {
        this.outboundItems = new ArrayList<>();
    }

    public static Outbound create(Long ordersId, Long warehouseId,
                                  OutboundType outboundType, List<OutboundLine> lines) {
        if (ordersId == null) throw new OutboundOrderIdRequiredException();
        if (warehouseId == null) throw new OutboundWarehouseRequiredException();
        if (outboundType == null) throw new OutboundTypeRequiredException();
        if (lines == null || lines.isEmpty()) throw new OutboundItemRequiredException();

        Outbound outbound = new Outbound(ordersId, warehouseId, outboundType);
        lines.forEach(line -> outbound.outboundItems.add(new OutboundItem(line)));
        return outbound;
    }

    public static Outbound reconstitute(Long id, Long ordersId, Long warehouseId,
                                        OutboundType outboundType, OutboundStatus outboundStatus,
                                        LocalDateTime outboundDate, List<OutboundItem> items) {
        Outbound outbound = new Outbound();
        outbound.id = id;
        outbound.ordersId = ordersId;
        outbound.warehouseId = warehouseId;
        outbound.outboundType = outboundType;
        outbound.outboundStatus = outboundStatus;
        outbound.outboundDate = outboundDate;
        outbound.outboundItems.addAll(items);
        return outbound;
    }

    public void allocate() {
        this.outboundStatus.validateAllocate();
        this.outboundStatus = OutboundStatus.ALLOCATED;
    }

    public void startProcessing() {
        this.outboundStatus.validateStartProcessing();
        this.outboundStatus = OutboundStatus.PROCESSING;
    }

    public void ship(LocalDateTime shippedAt) {
        this.outboundStatus.validateShip();
        boolean isAllPicked = this.outboundItems.stream().allMatch(OutboundItem::isFullyPicked);
        if (!isAllPicked) throw new OutboundIncompletePickingException();
        this.outboundStatus = OutboundStatus.SHIPPED;
        this.outboundDate = shippedAt;
    }

    public boolean cancel() {
        this.outboundStatus.validateCancel();
        boolean wasAllocated = (this.outboundStatus == OutboundStatus.ALLOCATED);
        this.outboundStatus = OutboundStatus.CANCELED;
        return wasAllocated;
    }

    public void changeItemPickedQuantity(Long inventoryId, int pickedQuantity) {
        this.outboundStatus.validatePicking();
        OutboundItem item = outboundItems.stream()
                .filter(i -> i.getInventoryId().equals(inventoryId))
                .findFirst()
                .orElseThrow(() -> new OutboundItemNotFoundException(inventoryId));
        item.changePickedQuantity(pickedQuantity);
    }

    public List<OutboundItem> getOutboundItems() {
        return Collections.unmodifiableList(outboundItems);
    }
}