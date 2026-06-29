package com.kb.cosmetic_wms.inspection.application.port.out;

public interface LotQueryPort {

    Long findLotIdByInboundAndManufacturerLot(Long inboundId, String manufacturerLotNumber);
}
