package com.kb.ordering.auth;

import com.kb.ordering.auth.application.port.in.dto.SignUpCommand;
import com.kb.ordering.auth.application.port.in.dto.SignUpResult;
import com.kb.ordering.auth.application.port.out.CredentialPort;
import com.kb.ordering.auth.application.port.out.RegisterMemberPort;
import com.kb.ordering.auth.application.port.out.dto.MemberInfo;
import com.kb.ordering.auth.application.port.out.dto.MemberRegistration;
import com.kb.ordering.auth.application.service.SignUpService;
import com.kb.ordering.auth.domain.exception.DuplicateLoginIdException;
import com.kb.ordering.auth.domain.model.Credential;
import com.kb.ordering.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class SignUpServiceTest {

    @InjectMocks
    private SignUpService signUpService;

    @Mock
    private CredentialPort credentialPort;

    @Mock
    private RegisterMemberPort registerMemberPort;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private SignUpCommand signUpCommand() {
        return new SignUpCommand(
                "admin01", "password123!", Role.ROLE_HEADQUARTERS,
                "홍길동", "admin@example.com", "010-1234-5678"
        );
    }

    @Test
    void 신규_회원_정보를_입력하면_회원과_자격증명이_함께_등록된다() {
        // given
        SignUpCommand command = signUpCommand();
        MemberInfo registeredMember = new MemberInfo(
                1L, command.memberName(), command.email(), command.phoneNumber(), command.role()
        );

        given(credentialPort.existsByLoginId(command.loginId())).willReturn(false);
        given(registerMemberPort.register(any(MemberRegistration.class))).willReturn(registeredMember);

        // when
        SignUpResult result = signUpService.signUp(command);

        // then
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.loginId()).isEqualTo(command.loginId());

        ArgumentCaptor<MemberRegistration> registrationCaptor = ArgumentCaptor.forClass(MemberRegistration.class);
        then(registerMemberPort).should().register(registrationCaptor.capture());
        assertThat(registrationCaptor.getValue().email()).isEqualTo(command.email());

        ArgumentCaptor<Credential> credentialCaptor = ArgumentCaptor.forClass(Credential.class);
        then(credentialPort).should().save(credentialCaptor.capture());
        Credential savedCredential = credentialCaptor.getValue();

        assertThat(savedCredential.getMemberId()).isEqualTo(1L);
        assertThat(savedCredential.getLoginId().value()).isEqualTo(command.loginId());
        assertThat(passwordEncoder.matches(command.password(), savedCredential.getEncodedPassword())).isTrue();
    }

    @Test
    void 이미_존재하는_로그인_ID로_가입을_요청하면_DuplicateLoginIdException_예외를_던지고_회원을_등록하지_않는다() {
        // given
        SignUpCommand command = signUpCommand();

        given(credentialPort.existsByLoginId(command.loginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> signUpService.signUp(command))
                .isInstanceOf(DuplicateLoginIdException.class);
        then(registerMemberPort).should(never()).register(any(MemberRegistration.class));
        then(credentialPort).should(never()).save(any(Credential.class));
    }
}
