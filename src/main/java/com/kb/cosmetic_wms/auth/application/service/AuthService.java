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
import org.springframework.stereotype.Service;

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
                .orElseThrow(InvalidRefreshTokenException::new);

        MemberIdentity identity = memberIdentityPort.findById(memberId)
                .orElseThrow(InvalidRefreshTokenException::new);

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
