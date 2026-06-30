package com.kb.cosmetic_wms.storage.application.port.in;

public record IncreaseSectionCapacityCommand(Long warehouseId, Long sectionId, int quantity) {}
