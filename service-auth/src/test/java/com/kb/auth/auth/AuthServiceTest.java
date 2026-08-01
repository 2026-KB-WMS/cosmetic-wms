package com.kb.auth.auth;

import com.kb.auth.auth.application.port.in.dto.LoginCommand;
import com.kb.auth.auth.application.port.in.dto.LoginResult;
import com.kb.auth.auth.application.port.in.dto.SignUpCommand;
import com.kb.auth.auth.application.port.in.dto.SignUpResult;
import com.kb.auth.auth.application.port.out.CredentialPort;
import com.kb.auth.auth.application.port.out.MemberPort;
import com.kb.auth.auth.application.port.out.TokenIssuer;
import com.kb.auth.auth.application.port.out.dto.MemberInfo;
import com.kb.auth.auth.application.port.out.dto.MemberRegistration;
import com.kb.auth.auth.application.service.AuthService;
import com.kb.auth.auth.domain.exception.DuplicateLoginIdException;
import com.kb.auth.auth.domain.exception.LoginFailedException;
import com.kb.auth.auth.domain.model.Credential;
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
    private MemberPort memberPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenIssuer tokenIssuer;

    @Nested
    class 로그인 {

        @Test
        void 올바른_아이디와_비밀번호로_로그인하면_토큰을_포함한_LoginResult를_반환한다() {
            // given
            Credential credential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");
            MemberInfo memberInfo = new MemberInfo(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);

            given(credentialPort.findByLoginId("user12345")).willReturn(Optional.of(credential));
            given(passwordEncoder.matches("Password1!", "$2a$10$encoded")).willReturn(true);
            given(memberPort.loadById(1L)).willReturn(memberInfo);
            given(tokenIssuer.issueAccessToken(1L, Role.ROLE_HEADQUARTERS)).willReturn("access.token");

            // when
            LoginResult result = authService.login(new LoginCommand("user12345", "Password1!"));

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.memberName()).isEqualTo("홍길동");
            assertThat(result.role()).isEqualTo(Role.ROLE_HEADQUARTERS);
            assertThat(result.accessToken()).isEqualTo("access.token");
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
    class 회원가입 {

        @Test
        void 올바른_정보로_회원가입하면_memberId와_loginId를_포함한_SignUpResult를_반환한다() {
            // given
            MemberInfo memberInfo = new MemberInfo(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);
            Credential savedCredential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");

            given(credentialPort.existsByLoginId("user12345")).willReturn(false);
            given(memberPort.register(any(MemberRegistration.class))).willReturn(memberInfo);
            given(passwordEncoder.encode("Password1!")).willReturn("$2a$10$encoded");
            given(credentialPort.save(any(Credential.class))).willReturn(savedCredential);

            // when
            SignUpResult result = authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            ));

            // then
            assertThat(result.memberId()).isEqualTo(1L);
            assertThat(result.loginId()).isEqualTo("user12345");
            assertThat(result.role()).isEqualTo(Role.ROLE_HEADQUARTERS);
        }

        @Test
        void 이미_존재하는_loginId로_가입하면_DuplicateLoginIdException을_던진다() {
            // given
            given(credentialPort.existsByLoginId("user12345")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            ))).isInstanceOf(DuplicateLoginIdException.class);
        }

        @Test
        void 회원가입_성공_시_Credential이_정확히_1회_저장된다() {
            // given
            MemberInfo memberInfo = new MemberInfo(1L, "홍길동", "test@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS);
            Credential savedCredential = Credential.reconstitute(1L, 1L, "user12345", "$2a$10$encoded");

            given(credentialPort.existsByLoginId("user12345")).willReturn(false);
            given(memberPort.register(any(MemberRegistration.class))).willReturn(memberInfo);
            given(passwordEncoder.encode(anyString())).willReturn("$2a$10$encoded");
            given(credentialPort.save(any(Credential.class))).willReturn(savedCredential);

            // when
            authService.signUp(new SignUpCommand(
                    "user12345", "Password1!", Role.ROLE_HEADQUARTERS, "홍길동", "test@example.com", "010-1234-5678"
            ));

            // then
            then(credentialPort).should(times(1)).save(any(Credential.class));
        }
    }
}