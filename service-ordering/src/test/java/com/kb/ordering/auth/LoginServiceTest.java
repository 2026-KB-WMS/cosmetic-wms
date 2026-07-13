package com.kb.ordering.auth;

import com.kb.ordering.auth.application.port.in.dto.LoginCommand;
import com.kb.ordering.auth.application.port.in.dto.LoginResult;
import com.kb.ordering.auth.application.port.out.CredentialPort;
import com.kb.ordering.auth.application.port.out.LoadMemberPort;
import com.kb.ordering.auth.application.port.out.TokenProvider;
import com.kb.ordering.auth.application.port.out.dto.MemberInfo;
import com.kb.ordering.auth.application.service.LoginService;
import com.kb.ordering.auth.domain.exception.LoginFailedException;
import com.kb.ordering.auth.domain.model.Credential;
import com.kb.ordering.auth.fixture.CredentialTestBuilder;
import com.kb.ordering.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private CredentialPort credentialPort;

    @Mock
    private LoadMemberPort loadMemberPort;

    @Mock
    private TokenProvider tokenProvider;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void 올바른_로그인_ID와_비밀번호를_입력하면_로그인에_성공하여_액세스_토큰을_반환한다() {
        // given
        LoginCommand command = new LoginCommand("admin01", "password123!");
        Credential credential = new CredentialTestBuilder()
                .loginId(command.loginId())
                .encodedPassword(passwordEncoder.encode(command.password()))
                .buildWithId(1L);
        MemberInfo member = new MemberInfo(
                1L, "홍길동", "admin@example.com", "010-1234-5678", Role.ROLE_HEADQUARTERS
        );

        given(credentialPort.findByLoginId(command.loginId())).willReturn(Optional.of(credential));
        given(loadMemberPort.loadById(credential.getMemberId())).willReturn(member);
        given(tokenProvider.issue(member.memberId(), member.role())).willReturn("access-token");

        // when
        LoginResult result = loginService.login(command);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.memberName()).isEqualTo("홍길동");
        assertThat(result.accessToken()).isEqualTo("access-token");
    }

    @Test
    void 존재하지_않는_로그인_ID를_입력하면_LoginFailedException_예외를_던진다() {
        // given
        LoginCommand command = new LoginCommand("unknown01", "password123!");

        given(credentialPort.findByLoginId(command.loginId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> loginService.login(command))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    void 올바른_로그인_ID를_입력해도_비밀번호가_일치하지_않으면_LoginFailedException_예외를_던진다() {
        // given
        LoginCommand command = new LoginCommand("admin01", "password20934!");
        Credential credential = new CredentialTestBuilder()
                .loginId(command.loginId())
                .encodedPassword(passwordEncoder.encode("password123!@"))
                .buildWithId(1L);

        given(credentialPort.findByLoginId(command.loginId())).willReturn(Optional.of(credential));

        // when & then
        assertThatThrownBy(() -> loginService.login(command))
                .isInstanceOf(LoginFailedException.class);
        then(loadMemberPort).should(never()).loadById(anyLong());
        then(tokenProvider).should(never()).issue(anyLong(), any(Role.class));
    }
}
