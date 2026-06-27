package com.kb.cosmetic_wms.inbound.domain.model;

import com.kb.cosmetic_wms.inbound.domain.enums.InboundStatus;
import com.kb.cosmetic_wms.inbound.domain.enums.InspectionStatus;
import com.kb.cosmetic_wms.inbound.domain.exception.*;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Inbound {

    private Long id;
    private InboundStatus inboundStatus;
    private final LocalDateTime inboundDate;
    private final Long warehouseId;
    private final Long partnerId;
    private final List<InboundItem> inboundItems;

    private Inbound(Long id, InboundStatus inboundStatus, LocalDateTime inboundDate,
                    Long warehouseId, Long partnerId, List<InboundItem> items) {
        this.id = id;
        this.inboundStatus = inboundStatus;
        this.inboundDate = inboundDate;
        this.warehouseId = warehouseId;
        this.partnerId = partnerId;
        this.inboundItems = new ArrayList<>(items);
    }

    public static Inbound create(LocalDateTime inboundDate, Long warehouseId, Long partnerId) {
        validateInboundDate(inboundDate);
        validateWarehouseId(warehouseId);
        validatePartnerId(partnerId);
        return new Inbound(null, InboundStatus.SCHEDULED, inboundDate, warehouseId, partnerId, List.of());
    }

    public static Inbound reconstitute(Long id, InboundStatus inboundStatus, LocalDateTime inboundDate,
                                       Long warehouseId, Long partnerId, List<InboundItem> items) {
        return new Inbound(id, inboundStatus, inboundDate, warehouseId, partnerId, items);
    }

    public InboundItem addItem(InboundLine line) {
        if (!this.inboundStatus.canAddItem()) {
            throw new InboundInvalidAddItemStatusException();
        }
        InboundItem item = new InboundItem(line);
        this.inboundItems.add(item);
        return item;
    }

    public InboundItem putawayItem(Long itemId, Long lotId, Long sectionId) {
        InboundItem item = findItemById(itemId);
        item.completePutaway(lotId, sectionId);
        return item;
    }

    public InboundItem approveItem(Long itemId) {
        InboundItem item = findItemById(itemId);
        item.changeToNormal();
        return item;
    }

    public InboundItem holdItem(Long itemId) {
        InboundItem item = findItemById(itemId);
        item.changeToHold();
        return item;
    }

    private InboundItem findItemById(Long itemId) {
        return inboundItems.stream()
                .filter(i -> itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(InboundItemNotFoundException::new);
    }

    public void startExecution() {
        if (!this.inboundStatus.canStart()) {
            throw new InboundInvalidStartStatusException(this.inboundStatus.getDescription());
        }
        this.inboundStatus = InboundStatus.IN_PROGRESS;
    }

    public void completeExecution() {
        if (!this.inboundStatus.canComplete()) {
            throw new InboundInvalidCompleteStatusException(this.inboundStatus.getDescription());
        }
        boolean isAllInspected = this.inboundItems.stream()
                .allMatch(item -> item.getInspectionStatus() == InspectionStatus.NORMAL
                        || item.getInspectionStatus() == InspectionStatus.HOLD);
        if (!isAllInspected) {
            throw new InboundInspectionIncompleteException();
        }
        this.inboundStatus = InboundStatus.COMPLETED;
    }

    public void cancel() {
        if (!this.inboundStatus.canCancel()) {
            throw new InboundInvalidCancelStatusException(this.inboundStatus.getDescription());
        }
        this.inboundStatus = InboundStatus.CANCELED;
    }

    public List<InboundItem> getInboundItems() {
        return Collections.unmodifiableList(inboundItems);
    }

    private static void validateInboundDate(LocalDateTime inboundDate) {
        if (inboundDate == null) {
            throw new InboundDateRequiredException();
        }
        if (inboundDate.toLocalDate().isBefore(LocalDate.now())) {
            throw new InboundPastDateException();
        }
    }

    private static void validateWarehouseId(Long warehouseId) {
        if (warehouseId == null) {
            throw new InboundWarehouseIdRequiredException();
        }
    }

    private static void validatePartnerId(Long partnerId) {
        if (partnerId == null) {
            throw new InboundPartnerIdRequiredException();
        }
    }
}