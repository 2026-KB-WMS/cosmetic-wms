package com.kb.cosmetic_wms.outbound.application.port.out;

public interface OrderPreparationPort {

    void startPreparation(Long orderId);
}
