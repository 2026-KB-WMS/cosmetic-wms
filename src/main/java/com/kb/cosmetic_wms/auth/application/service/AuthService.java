package com.kb.cosmetic_wms.auth.application.service;

import com.kb.cosmetic_wms.auth.application.port.in.LoginCommand;
import com.kb.cosmetic_wms.auth.application.port.in.LoginUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.LogoutUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.RefreshTokenCommand;
import com.kb.cosmetic_wms.auth.application.port.in.ReissueTokenUseCase;
import com.kb.cosmetic_wms.auth.application.port.in.TokenResult;
import com.kb.cosmetic_wms.auth.application.port.out.AccessTokenPort;
import com.kb.cosmetic_wms.auth.application.port.out.IssuedAccessToken;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentity;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentityPort;
import com.kb.cosmetic_wms.auth.application.port.out.RefreshTokenPort;
import com.kb.cosmetic_wms.auth.domain.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase, ReissueTokenUseCase, LogoutUseCase {

    private final MemberIdentityPort memberIdentityPort;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenPort refreshTokenPort;

    @Override
    public TokenResult login(LoginCommand command) {
        MemberIdentity identity = memberIdentityPort.authenticate(command.loginId(), command.password());
        return issueTokens(identity);
    }

    @Override
    public TokenResult reissue(RefreshTokenCommand command) {
        Long memberId = refreshTokenPort.consume(command.refreshToken())
                .orElseThrow(() -> {
                    log.warn("유효하지 않은 리프레시 토큰으로 재발급이 시도되었습니다.");
                    return new InvalidRefreshTokenException();
                });

        MemberIdentity identity = memberIdentityPort.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("리프레시 토큰의 회원을 찾을 수 없습니다. memberId={}", memberId);
                    return new InvalidRefreshTokenException();
                });

        return issueTokens(identity);
    }

    @Override
    public void logout(RefreshTokenCommand command) {
        refreshTokenPort.revoke(command.refreshToken());
    }

    private TokenResult issueTokens(MemberIdentity identity) {
        IssuedAccessToken accessToken = accessTokenPort.issue(identity);
        String refreshToken = refreshTokenPort.issue(identity.memberId());
        return new TokenResult(accessToken.token(), refreshToken, accessToken.expiresInSeconds());
    }
}
