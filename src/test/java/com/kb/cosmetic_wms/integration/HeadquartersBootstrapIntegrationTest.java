package com.kb.cosmetic_wms.integration;

import com.kb.cosmetic_wms.member.adapter.in.bootstrap.HeadquartersBootstrapRunner;
import com.kb.cosmetic_wms.member.application.port.out.MemberPort;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(properties = {
        "bootstrap.headquarters.login-id=hq-boot",
        "bootstrap.headquarters.password=Password123!",
        "bootstrap.headquarters.member-name=본사관리자",
        "bootstrap.headquarters.email=hq-boot@example.com",
        "bootstrap.headquarters.phone-number=010-9999-8888"
})
class HeadquartersBootstrapIntegrationTest {

    @Autowired
    private MemberPort memberPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HeadquartersBootstrapRunner runner;

    @Test
    void 환경변수로_기동하면_본사_관리자_계정이_DB에_저장되고_로그인_가능한_비밀번호로_인코딩된다() {
        Member member = memberPort.findByLoginId("hq-boot").orElseThrow();

        assertThat(member.getRole()).isEqualTo(Role.ROLE_HEADQUARTERS);
        assertThat(member.getMemberName()).isEqualTo("본사관리자");
        assertThat(passwordEncoder.matches("Password123!", member.getEncodedPassword())).isTrue();
    }

    @Test
    void 재기동처럼_다시_실행해도_예외_없이_계정을_중복_생성하지_않는다() {
        assertThatCode(() -> runner.run(new DefaultApplicationArguments())).doesNotThrowAnyException();

        assertThat(memberPort.existsByRole(Role.ROLE_HEADQUARTERS)).isTrue();
        assertThat(memberPort.findByLoginId("hq-boot")).isPresent();
    }
}
