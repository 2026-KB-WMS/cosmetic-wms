package com.kb.cosmetic_wms.domain.inventory.entity;

public record SplitResult(Inventory original, Inventory result, boolean wasSplit) {

    public static SplitResult noSplit(Inventory inventory) {
        return new SplitResult(inventory, inventory, false);
    }

    public static SplitResult split(Inventory original, Inventory splitOff) {
        return new SplitResult(original, splitOff, true);
    }
}
