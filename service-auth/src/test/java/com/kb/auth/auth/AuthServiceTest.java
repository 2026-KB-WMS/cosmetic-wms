package com.kb.auth.auth;

import com.kb.auth.auth.application.port.in.dto.LoginCommand;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.LogoutCommand;
import com.kb.auth.auth.application.port.in.dto.ReissueCommand;
import com.kb.auth.auth.application.port.in.dto.ReissueResult;
import com.kb.auth.auth.application.port.in.dto.SignUpCommand;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.RefreshTokenPort;
import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.TokenParser;
import com.kb.auth.auth.application.service.AuthService;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.exception.HeadquartersRoleNotAllowedException;
import com.kb.auth.auth.domain.exception.InvalidRefreshTokenException;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.auth.domain.model.Credential;
import com.kb.auth.member.application.port.in.FindMemberUseCase;
import com.kb.auth.member.application.port.in.RegisterMemberUseCase;
import com.kb.auth.member.application.port.in.dto.MemberResult;
import com.kb.auth.member.application.port.in.dto.RegisterMemberCommand;
import com.kb.auth.member.domain.model.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private CredentialPort credentialPort;

    @Mock
    private FindMemberUseCase findMemberUseCase;

    @Mock
    private RegisterMemberUseCase registerMemberUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private TokenParser tokenParser;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Nested
    class 로그인 {

        @Test
        void 올바른_아이디와_비밀번호로_로그인하면_액세스_토큰과_리프레시_토큰을_포함한_LoginResult를_반환한다() {
            // given
            Credential credential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);

            given(credentialPort.findByLoginId("user12345")).willReturn(Optional.of(credential));
            given(passwordEncoder.matches("Password1!", "$2a$10$encoded")).willReturn(true);
            given(findMemberUseCase.findById(1L)).willReturn(memberResult);
            given(tokenIssuer.issueAccessToken(1L, Role.ROLE_HEADQUARTERS)).willReturn("access.token");
            given(tokenIssuer.issueRefreshToken(1L)).willReturn("refresh.token");

            // when
            LoginResult result = authService.login(new LoginCommand("user12345", "Password1!"));

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.memberName()).isEqualTo("홍길동");
            assertThat(result.role()).isEqualTo(Role.ROLE_HEADQUARTERS);
            assertThat(result.accessToken()).isEqualTo("access.token");
            assertThat(result.refreshToken()).isEqualTo("refresh.token");
        }

        @Test
        void 로그인_성공_시_리프레시_토큰이_저장소에_1회_저장된다() {
            // given
            Credential credential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);

            given(credentialPort.findByLoginId("user12345")).willReturn(Optional.of(credential));
            given(passwordEncoder.matches("Password1!", "$2a$10$encoded")).willReturn(true);
            given(findMemberUseCase.findById(1L)).willReturn(memberResult);
            given(tokenIssuer.issueAccessToken(1L, Role.ROLE_HEADQUARTERS)).willReturn("access.token");
            given(tokenIssuer.issueRefreshToken(1L)).willReturn("refresh.token");

            // when
            authService.login(new LoginCommand("user12345", "Password1!"));

            // then
            then(refreshTokenPort).should(times(1)).save(1L, "refresh.token");
        }

        @Test
        void 존재하지_않는_loginId로_로그인하면_LoginFailedException을_던진다() {
            // given
            given(credentialPort.findByLoginId(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(new LoginCommand("unknown", "Password1!")))
                    .isInstanceOf(LoginFailedException.class);
        }

        @Test
        void 비밀번호가_일치하지_않으면_LoginFailedException을_던진다() {
            // given
            Credential credential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");
            given(credentialPort.findByLoginId("user12345")).willReturn(Optional.of(credential));
            given(passwordEncoder.matches("wrongPassword", "$2a$10$encoded")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(new LoginCommand("user12345", "wrongPassword")))
                    .isInstanceOf(LoginFailedException.class);
        }
    }

    @Nested
    class 로그아웃 {

        @Test
        void 유효한_리프레시_토큰으로_로그아웃하면_Redis에서_토큰이_삭제된다() {
            // given
            given(tokenParser.extractMemberId("valid.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.of("valid.refresh.token"));

            // when
            authService.logout(new LogoutCommand("valid.refresh.token"));

            // then
            then(refreshTokenPort).should(times(1)).delete(1L);
        }

        @Test
        void 토큰_파싱_실패_시_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("malformed.token")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.logout(new LogoutCommand("malformed.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        void 토큰_파싱_실패_시_Redis와_상호작용하지_않는다() {
            // given
            given(tokenParser.extractMemberId("malformed.token")).willReturn(Optional.empty());

            // when
            try {
                authService.logout(new LogoutCommand("malformed.token"));
            } catch (InvalidRefreshTokenException ignored) {
            }

            // then
            then(refreshTokenPort).shouldHaveNoInteractions();
        }

        @Test
        void Redis에_저장된_토큰이_없으면_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("valid.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.logout(new LogoutCommand("valid.refresh.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        void 저장된_토큰과_입력_토큰이_다르면_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("stale.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.of("current.refresh.token"));

            // when & then
            assertThatThrownBy(() -> authService.logout(new LogoutCommand("stale.refresh.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    class 토큰_재발급 {

        @Test
        void 유효한_리프레시_토큰으로_재발급하면_새_액세스_토큰과_새_리프레시_토큰을_반환한다() {
            // given
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);

            given(tokenParser.extractMemberId("valid.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.of("valid.refresh.token"));
            given(findMemberUseCase.findById(1L)).willReturn(memberResult);
            given(tokenIssuer.issueAccessToken(1L, Role.ROLE_HEADQUARTERS)).willReturn("new.access.token");
            given(tokenIssuer.issueRefreshToken(1L)).willReturn("new.refresh.token");

            // when
            ReissueResult result = authService.reissue(new ReissueCommand("valid.refresh.token"));

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.accessToken()).isEqualTo("new.access.token");
            assertThat(result.refreshToken()).isEqualTo("new.refresh.token");
        }

        @Test
        void 재발급_성공_시_기존_토큰을_덮어쓰는_방식으로_새_토큰을_저장하고_delete는_호출되지_않는다() {
            // given
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);

            given(tokenParser.extractMemberId("valid.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.of("valid.refresh.token"));
            given(findMemberUseCase.findById(1L)).willReturn(memberResult);
            given(tokenIssuer.issueAccessToken(1L, Role.ROLE_HEADQUARTERS)).willReturn("new.access.token");
            given(tokenIssuer.issueRefreshToken(1L)).willReturn("new.refresh.token");

            // when
            authService.reissue(new ReissueCommand("valid.refresh.token"));

            // then
            then(refreshTokenPort).should(times(0)).delete(1L);
            then(refreshTokenPort).should(times(1)).save(1L, "new.refresh.token");
        }

        @Test
        void 토큰_파싱_실패_시_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("malformed.token")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(new ReissueCommand("malformed.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        void Redis에_저장된_토큰이_없으면_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("valid.refresh.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue(new ReissueCommand("valid.refresh.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }

        @Test
        void 저장된_토큰과_입력_토큰이_다르면_InvalidRefreshTokenException을_던진다() {
            // given
            given(tokenParser.extractMemberId("stolen.token")).willReturn(Optional.of(1L));
            given(refreshTokenPort.find(1L)).willReturn(Optional.of("original.token"));

            // when & then
            assertThatThrownBy(() -> authService.reissue(new ReissueCommand("stolen.token")))
                    .isInstanceOf(InvalidRefreshTokenException.class);
        }
    }

    @Nested
    class 회원가입 {

        @Test
        void 올바른_정보로_회원가입하면_memberId와_loginId를_포함한_SignUpResult를_반환한다() {
            // given
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_WAREHOUSE_MANAGER);
            Credential savedCredential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");

            given(credentialPort.existsByLoginId("user12345")).willReturn(false);
            given(registerMemberUseCase.register(any(RegisterMemberCommand.class))).willReturn(memberResult);
            given(passwordEncoder.encode("Password1!")).willReturn("$2a$10$encoded");
            given(credentialPort.save(any(Credential.class))).willReturn(savedCredential);

            // when
            SignUpResult result = authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            ));

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.loginId()).isEqualTo("user12345");
            assertThat(result.role()).isEqualTo(Role.ROLE_WAREHOUSE_MANAGER);
        }

        @Test
        void ROLE_HEADQUARTERS로_가입하면_HeadquartersRoleNotAllowedException을_던진다() {
            // when & then
            assertThatThrownBy(() -> authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            ))).isInstanceOf(HeadquartersRoleNotAllowedException.class);
        }

        @Test
        void ROLE_HEADQUARTERS_차단_시_loginId_중복_검사가_호출되지_않는다() {
            // when
            try {
                authService.signUp(new SignUpCommand(
                        "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
                ));
            } catch (HeadquartersRoleNotAllowedException ignored) {
            }

            // then
            then(credentialPort).shouldHaveNoInteractions();
        }

        @Test
        void 이미_존재하는_loginId로_가입하면_DuplicateLoginIdException을_던진다() {
            // given
            given(credentialPort.existsByLoginId("user12345")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            ))).isInstanceOf(DuplicateLoginIdException.class);
        }

        @Test
        void 회원가입_성공_시_Credential이_정확히_1회_저장된다() {
            // given
            MemberResult memberResult = new MemberResult(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_WAREHOUSE_MANAGER);
            Credential savedCredential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");

            given(credentialPort.existsByLoginId("user12345")).willReturn(false);
            given(registerMemberUseCase.register(any(RegisterMemberCommand.class))).willReturn(memberResult);
            given(passwordEncoder.encode(anyString())).willReturn("$2a$10$encoded");
            given(credentialPort.save(any(Credential.class))).willReturn(savedCredential);

            // when
            authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_WAREHOUSE_MANAGER, "홍길동", "test@example.com", "010-1234-5678"
            ));

            // then
            then(credentialPort).should(times(1)).save(any(Credential.class));
        }
    }
}
