package com.kb.cosmetic_wms.auth;

import com.kb.cosmetic_wms.auth.application.port.in.LoginCommand;
import com.kb.cosmetic_wms.auth.application.port.in.RefreshTokenCommand;
import com.kb.cosmetic_wms.auth.application.port.in.TokenResult;
import com.kb.cosmetic_wms.auth.application.port.out.AccessTokenPort;
import com.kb.cosmetic_wms.auth.application.port.out.IssuedAccessToken;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentity;
import com.kb.cosmetic_wms.auth.application.port.out.MemberIdentityPort;
import com.kb.cosmetic_wms.auth.application.port.out.RefreshTokenPort;
import com.kb.cosmetic_wms.auth.application.service.AuthService;
import com.kb.cosmetic_wms.auth.domain.exception.InvalidRefreshTokenException;
import com.kb.cosmetic_wms.support.LogCapture;
import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberIdentityPort memberIdentityPort;

    @Mock
    private AccessTokenPort accessTokenPort;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    private final MemberIdentity identity = new MemberIdentity(7L, "ROLE_WAREHOUSE_MANAGER");

    @Test
    void 올바른_자격_증명으로_로그인하면_액세스_토큰과_리프레시_토큰을_발급한다() {
        // given
        given(memberIdentityPort.authenticate("admin01", "password123!")).willReturn(identity);
        given(accessTokenPort.issue(identity)).willReturn(new IssuedAccessToken("access-token", 1800));
        given(refreshTokenPort.issue(7L)).willReturn("refresh-token");

        // when
        TokenResult result = authService.login(new LoginCommand("admin01", "password123!"));

        // then
        assertThat(result).isEqualTo(new TokenResult("access-token", "refresh-token", 1800));
    }

    @Test
    void 인증에_실패하면_토큰을_발급하지_않고_예외가_그대로_전달된다() {
        // given
        given(memberIdentityPort.authenticate("admin01", "wrong"))
                .willThrow(new IllegalStateException("인증 실패"));

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginCommand("admin01", "wrong")))
                .isInstanceOf(IllegalStateException.class);
        verify(accessTokenPort, never()).issue(identity);
        verify(refreshTokenPort, never()).issue(7L);
    }

    @Test
    void 유효한_리프레시_토큰으로_재발급하면_기존_토큰을_폐기하고_새_토큰_쌍을_발급한다() {
        // given
        given(refreshTokenPort.consume("old-refresh")).willReturn(Optional.of(7L));
        given(memberIdentityPort.findById(7L)).willReturn(Optional.of(identity));
        given(accessTokenPort.issue(identity)).willReturn(new IssuedAccessToken("new-access", 1800));
        given(refreshTokenPort.issue(7L)).willReturn("new-refresh");

        // when
        TokenResult result = authService.reissue(new RefreshTokenCommand("old-refresh"));

        // then
        assertThat(result).isEqualTo(new TokenResult("new-access", "new-refresh", 1800));
    }

    @Test
    void 저장소에_없는_리프레시_토큰으로_재발급하면_InvalidRefreshTokenException_예외를_던진다() {
        // given
        given(refreshTokenPort.consume("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(new RefreshTokenCommand("unknown")))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(accessTokenPort, never()).issue(identity);
    }

    @Test
    void 토큰은_유효하지만_회원이_사라졌으면_InvalidRefreshTokenException_예외를_던진다() {
        // given
        given(refreshTokenPort.consume("orphan")).willReturn(Optional.of(99L));
        given(memberIdentityPort.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.reissue(new RefreshTokenCommand("orphan")))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenPort, never()).issue(99L);
    }

    @Test
    void 로그아웃하면_리프레시_토큰을_폐기한다() {
        // when
        authService.logout(new RefreshTokenCommand("refresh-token"));

        // then
        verify(refreshTokenPort).revoke("refresh-token");
    }

    @Test
    void 유효하지_않은_리프레시_토큰으로_재발급하면_WARN_로그를_남기되_토큰_값은_남기지_않는다() {
        // given
        given(refreshTokenPort.consume("secret-refresh-value")).willReturn(Optional.empty());

        try (LogCapture logs = LogCapture.of(AuthService.class)) {
            // when
            assertThatThrownBy(() -> authService.reissue(new RefreshTokenCommand("secret-refresh-value")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            // then
            assertThat(logs.messages(Level.WARN)).hasSize(1);
            assertThat(logs.allMessages()).noneMatch(message -> message.contains("secret-refresh-value"));
        }
    }

    @Test
    void 토큰은_유효하지만_회원이_사라졌으면_회원_ID와_함께_WARN_로그를_남긴다() {
        // given
        given(refreshTokenPort.consume("orphan")).willReturn(Optional.of(99L));
        given(memberIdentityPort.findById(99L)).willReturn(Optional.empty());

        try (LogCapture logs = LogCapture.of(AuthService.class)) {
            // when
            assertThatThrownBy(() -> authService.reissue(new RefreshTokenCommand("orphan")))
                    .isInstanceOf(InvalidRefreshTokenException.class);

            // then
            assertThat(logs.messages(Level.WARN)).singleElement().satisfies(message ->
                    assertThat(message).contains("memberId=99").doesNotContain("orphan"));
        }
    }
}
