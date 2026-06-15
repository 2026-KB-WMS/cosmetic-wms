package com.kb.cosmetic_wms.domain.member.entity;

import com.kb.cosmetic_wms.domain.member.MemberConstants;
import com.kb.cosmetic_wms.domain.member.enums.Role;
import com.kb.cosmetic_wms.domain.member.exception.InvalidEmailException;
import com.kb.cosmetic_wms.domain.member.exception.InvalidPhoneNumberException;
import com.kb.cosmetic_wms.domain.member.exception.MemberValidationException;
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

    private Member(String loginId, String encodedPassword, Role role,
                   String memberName, String email, String phoneNumber
    ) {
        this.loginId = loginId;
        this.encodedPassword = encodedPassword;
        this.role = role;
        this.memberName = memberName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public static Member create(
            String loginId,
            String encodedPassword,
            Role role,
            String memberName,
            String email,
            String phoneNumber
    ) {
        validateLoginId(loginId);
        validateEncodedPassword(encodedPassword);
        validateEmail(email);
        validatePhoneNumber(phoneNumber);

        return new Member(
                loginId,
                encodedPassword,
                role,
                memberName,
                email,
                phoneNumber
        );
    }

    private static void validateEmail(String email) {
        String emailRegex = MemberConstants.EMAIL_REGEX;
        if (email == null || !Pattern.matches(emailRegex, email)) {
            throw new InvalidEmailException();
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        String phoneNumberRegex = MemberConstants.PHONE_NUMBER_REGEX;
        if (phoneNumber == null || !Pattern.matches(phoneNumberRegex, phoneNumber)) {
            throw new InvalidPhoneNumberException();
        }
    }

    private static void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new MemberValidationException();
        }
        if (loginId.length() < MemberConstants.LOGIN_ID_MIN_LENGTH ||
                loginId.length() > MemberConstants.LOGIN_ID_MAX_LENGTH) {
            throw new MemberValidationException();
        }
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new MemberValidationException();
        }
    }
}
