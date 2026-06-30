package com.kb.cosmetic_wms.inspection.application.port.out;

import com.kb.cosmetic_wms.inbound.application.event.InboundCompletedEvent;

public interface InspectionCreationFailurePort {

    void save(InboundCompletedEvent event, InboundCompletedEvent.LineSnapshot line, String errorMessage);
}
