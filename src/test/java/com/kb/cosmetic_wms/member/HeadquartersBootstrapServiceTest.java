package com.kb.cosmetic_wms.member;

import com.kb.cosmetic_wms.member.application.port.in.BootstrapHeadquartersCommand;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.application.service.HeadquartersBootstrapService;
import com.kb.cosmetic_wms.member.domain.exception.DuplicateMemberException;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HeadquartersBootstrapServiceTest {

    @InjectMocks
    private HeadquartersBootstrapService service;

    @Mock
    private MemberPort memberPort;

    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private final BootstrapHeadquartersCommand command = new BootstrapHeadquartersCommand(
            "hq-admin", "Password123!", "본사관리자", "hq@example.com", "010-1111-2222"
    );

    @Test
    void 본사_관리자가_없으면_본사_권한_계정을_생성하고_비밀번호는_인코딩하여_저장한다() {
        // given
        given(memberPort.existsByRole(Role.ROLE_HEADQUARTERS)).willReturn(false);
        given(memberPort.existsByLoginId(command.loginId())).willReturn(false);

        // when
        boolean created = service.bootstrap(command);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberPort).save(captor.capture());
        Member saved = captor.getValue();

        assertThat(created).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.ROLE_HEADQUARTERS);
        assertThat(saved.getLoginId().value()).isEqualTo("hq-admin");
        assertThat(saved.getEncodedPassword()).isNotEqualTo("Password123!");
        assertThat(passwordEncoder.matches("Password123!", saved.getEncodedPassword())).isTrue();
    }

    @Test
    void 본사_관리자가_이미_있으면_아무것도_저장하지_않고_false를_반환한다() {
        // given
        given(memberPort.existsByRole(Role.ROLE_HEADQUARTERS)).willReturn(true);

        // when
        boolean created = service.bootstrap(command);

        // then
        assertThat(created).isFalse();
        verify(memberPort, never()).save(any(Member.class));
    }

    @Test
    void 본사_관리자가_없어도_로그인_ID가_이미_사용_중이면_DuplicateMemberException_예외를_던진다() {
        // given
        given(memberPort.existsByRole(Role.ROLE_HEADQUARTERS)).willReturn(false);
        given(memberPort.existsByLoginId(command.loginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> service.bootstrap(command))
                .isInstanceOf(DuplicateMemberException.class);
        verify(memberPort, never()).save(any(Member.class));
    }
}
