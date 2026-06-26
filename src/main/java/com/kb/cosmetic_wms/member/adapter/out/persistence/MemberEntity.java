package com.kb.cosmetic_wms.member.adapter.out.persistence;

import com.kb.cosmetic_wms.global.common.BaseEntity;
import com.kb.cosmetic_wms.member.domain.model.Member;
import com.kb.cosmetic_wms.member.domain.model.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uq_email", columnNames = "email"),
                @UniqueConstraint(name = "uq_phone_number", columnNames = "phone_number")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String encodedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    private MemberEntity(String loginId, String encodedPassword, Role role,
                         String memberName, String email, String phoneNumber) {
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    static MemberEntity fromDomain(Member member) {
        return new MemberEntity(
                member.getLoginId().value(),
                member.getEncodedPassword(),
                member.getRole(),
                member.getMemberName(),
                member.getEmail().value(),
                member.getPhoneNumber().value()
        );
    }

    Member toDomain() {
        return Member.reconstitute(id, loginId, encodedPassword, role, memberName, email, phoneNumber);
    }
}