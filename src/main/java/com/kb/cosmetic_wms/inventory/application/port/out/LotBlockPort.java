package com.kb.cosmetic_wms.inventory.application.port.out;

public interface LotBlockPort {

    void block(Long lotId, String status);

    void release(Long lotId);

    boolean isBlocked(Long lotId);
}
