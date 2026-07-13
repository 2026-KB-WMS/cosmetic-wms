package com.kb.ordering.member.adapter.out.persistence;

import com.kb.common.jpa.BaseEntity;
import com.kb.ordering.member.domain.model.Member;
import com.kb.ordering.member.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    private MemberEntity(Role role, String memberName, String email, String phoneNumber) {
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    static MemberEntity fromDomain(Member member) {
        return new MemberEntity(
                member.getRole(),
                member.getMemberName(),
                member.getEmail().value(),
                member.getPhoneNumber().value()
        );
    }

    Member toDomain() {
        return Member.reconstitute(id, role, memberName, email, phoneNumber);
    }
}
