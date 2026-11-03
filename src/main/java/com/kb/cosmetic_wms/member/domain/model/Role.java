package com.kb.cosmetic_wms.member.domain.model;

public enum Role {
    ROLE_HEADQUARTERS,
    ROLE_WAREHOUSE_MANAGER,
    ROLE_FRANCHISE_MANAGER;

    public boolean isSelfRegistrable() {
        return this != ROLE_HEADQUARTERS;
    }
}
