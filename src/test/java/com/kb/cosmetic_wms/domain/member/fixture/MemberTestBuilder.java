package com.kb.cosmetic_wms.domain.member.fixture;

import com.kb.cosmetic_wms.domain.member.entity.Member;
import com.kb.cosmetic_wms.domain.member.enums.Role;

public class MemberTestBuilder {
    private String loginId = "admin01";
    private String password = "password123!";
    private Role role = Role.ROLE_HEADQUARTERS;
    private String memberName = "홍길동";
    private String email = "admin@example.com";
    private String phoneNumber = "010-1234-5678";

    public MemberTestBuilder loginId(String loginId) {
        this.loginId = loginId;
        return this;
    }

    public MemberTestBuilder password(String password) {
        this.password = password;
        return this;
    }

    public MemberTestBuilder role(Role role) {
        this.role = role;
        return this;
    }

    public MemberTestBuilder memberName(String memberName) {
        this.memberName = memberName;
        return this;
    }

    public MemberTestBuilder email(String email) {
        this.email = email;
        return this;
    }

    public MemberTestBuilder phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public Member build() {
        return Member.create(
                loginId,
                password,
                role,
                memberName,
                email,
                phoneNumber
        );
    }
}
