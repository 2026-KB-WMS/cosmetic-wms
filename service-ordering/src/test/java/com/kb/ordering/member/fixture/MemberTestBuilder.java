package com.kb.ordering.member.fixture;

import com.kb.ordering.member.domain.model.Member;
import com.kb.ordering.member.domain.model.Role;

public class MemberTestBuilder {
    private Role role = Role.ROLE_HEADQUARTERS;
    private String memberName = "홍길동";
    private String email = "admin@example.com";
    private String phoneNumber = "010-1234-5678";

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
        return Member.create(role, memberName, email, phoneNumber);
    }

    public Member buildWithId(Long id) {
        return Member.reconstitute(id, role, memberName, email, phoneNumber);
    }
}
