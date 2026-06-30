package com.kb.cosmetic_wms.putaway.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PutawayStatus {
    PENDING("대기 중"),
    COMPLETED("완료");

    private final String description;

    public boolean canTransitionTo(PutawayStatus next) {
        return this == PENDING && next == COMPLETED;
    }
}
