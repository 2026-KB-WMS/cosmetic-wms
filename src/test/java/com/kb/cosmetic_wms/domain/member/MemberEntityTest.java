package com.kb.cosmetic_wms.domain.member;

import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequest;
import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MemberEntityTest {

    @Test
    void 올바른_입력값으로_가입_시_정상적으로_엔티티가_생성된다() {
        // given
        MemberSignUpRequest request = new MemberSignUpRequest(
                "admin01",
                "password123!",
                Role.ROLE_HQ,
                "홍길동",
                "admin@example.com",
                "010-1234-5678"
        );

        // when
        Member member = Member.create(request);

        // then
        Assertions.assertThat(member.getLoginId()).isEqualTo("admin01");
        Assertions.assertThat(member.getRole()).isEqualTo(Role.ROLE_HQ);
        Assertions.assertThat(member.getMemberName()).isEqualTo("홍길동");
    }
}
