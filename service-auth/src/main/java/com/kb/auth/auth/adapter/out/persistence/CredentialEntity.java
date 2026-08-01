package com.kb.auth.auth.adapter.out.persistence;

import com.kb.common.jpa.BaseEntity;
import com.kb.auth.auth.domain.model.Credential;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "credential",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uq_credential_member", columnNames = "member_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class CredentialEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credential_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String encodedPassword;

    private CredentialEntity(Long memberId, String loginId, String encodedPassword) {
        this.memberId = memberId;
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
    }

    static CredentialEntity fromDomain(Credential credential) {
        return new CredentialEntity(
                credential.getMemberId(),
                credential.getLoginId().value(),
                credential.getEncodedPassword()
        );
    }

    Credential toDomain() {
        return Credential.reconstitute(id, memberId, loginId, encodedPassword);
    }
}
