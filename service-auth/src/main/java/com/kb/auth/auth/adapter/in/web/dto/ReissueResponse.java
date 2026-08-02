package com.kb.auth.auth.adapter.in.web.dto;

import com.kb.auth.auth.application.port.in.dto.ReissueResult;

public record ReissueResponse(
        Long memberId,
        String accessToken,
        String refreshToken
) {
    public static ReissueResponse from(ReissueResult result) {
        return new ReissueResponse(result.memberId(), result.accessToken(), result.refreshToken());
    }
}
