package com.kb.cosmetic_wms.domain.member.entity;

import com.kb.cosmetic_wms.domain.member.dto.MemberSignUpRequest;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String loginId;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String memberName;
    private String email;
    private String phoneNumber;

    private Member(String loginId, String password, Role role, String memberName, String email, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(MemberSignUpRequest request) {
        return new Member(
                request.loginId(),
                request.password(),
                request.role(),
                request.memberName(),
                request.email(),
                request.phoneNumber()
        );
    }
}
