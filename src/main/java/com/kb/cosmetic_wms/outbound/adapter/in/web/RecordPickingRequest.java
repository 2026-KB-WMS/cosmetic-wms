package com.kb.cosmetic_wms.outbound.adapter.in.web;

import jakarta.validation.constraints.NotNull;

public record RecordPickingRequest(@NotNull Integer pickedQuantity) {
}
