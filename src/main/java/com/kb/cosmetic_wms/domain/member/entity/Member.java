package com.kb.cosmetic_wms.domain.member.entity;

import com.kb.cosmetic_wms.domain.member.MemberConstants;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import com.kb.cosmetic_wms.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

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
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    private Member(String loginId, String password, Role role, String memberName, String email, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(
            String loginId,
            String password,
            Role role,
            String memberName,
            String email,
            String phoneNumber
    ) {
        validateLoginId(loginId);
        validatePassword(password);
        validateEmail(email);
        validatePhoneNumber(phoneNumber);

        return new Member(
                loginId,
                password,
                role,
                memberName,
                email,
                phoneNumber
        );
    }

    private static void validateEmail(String email) {
        String emailRegex = MemberConstants.EMAIL_REGEX;
        if (email == null || !Pattern.matches(emailRegex, email)) {
            throw new IllegalArgumentException(MemberConstants.INVALID_EMAIL_MESSAGE);
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        String phoneNumberRegex = MemberConstants.PHONE_NUMBER_REGEX;
        if (phoneNumber == null || !Pattern.matches(phoneNumberRegex, phoneNumber)) {
            throw new IllegalArgumentException(MemberConstants.INVALID_PHONE_NUMBER_MESSAGE);
        }
    }

    private static void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException(MemberConstants.LOGIN_ID_REQUIRED_MESSAGE);
        }
        if (loginId.length() < MemberConstants.LOGIN_ID_MIN_LENGTH ||
                loginId.length() > MemberConstants.LOGIN_ID_MAX_LENGTH) {
            throw new IllegalArgumentException(MemberConstants.INVALID_LOGIN_ID_LENGTH_MESSAGE);
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(MemberConstants.PASSWORD_REQUIRED_MESSAGE);
        }
    }
}
